/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.core;

import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.processes.securityanalysis.DummySecurityAnalysisService;
import org.gridsuite.process.worker.server.processes.securityanalysis.SecurityAnalysisProcess;
import org.gridsuite.process.worker.server.services.NetworkConversionService;
import org.gridsuite.process.worker.server.services.NotificationService;
import org.gridsuite.process.worker.server.services.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractProcessTest {
    @Mock
    private StepExecutionService<SecurityAnalysisConfig> stepExecutionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NetworkConversionService networkConversionService;

    @Mock
    private DummySecurityAnalysisService securityAnalysisService;

    @Mock
    private ProcessExecutionContext<SecurityAnalysisConfig> processContext;

    @Mock
    private SecurityAnalysisConfig config;

    @Captor
    private ArgumentCaptor<ProcessStep<SecurityAnalysisConfig>> stepCaptor;

    private SecurityAnalysisProcess process;

    private static final UUID EXECUTION_ID = UUID.randomUUID();
    private static final UUID CASE_UUID = UUID.randomUUID();
    private static final UUID PARAMS_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        process = new SecurityAnalysisProcess(
            stepExecutionService,
            notificationService,
            networkConversionService,
            securityAnalysisService
        );

        when(config.executionId()).thenReturn(EXECUTION_ID);
        when(config.caseUuid()).thenReturn(CASE_UUID);
        when(config.parametersUuid()).thenReturn(PARAMS_UUID);
        when(config.contingencies()).thenReturn(List.of("contingency1", "contingency2"));
        when(config.modificationUuids()).thenReturn(List.of());

        when(processContext.getConfig()).thenReturn(config);
        when(processContext.getExecutionId()).thenReturn(EXECUTION_ID);
        when(processContext.createStepContext(any())).thenCallRealMethod();
    }

    @Test
    void executeShouldExecuteAllStepsSuccessfullyWhenNoErrors() {
        // Given
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();
        UUID step3Id = UUID.randomUUID();

        when(processContext.getLastExecutedStepId())
            .thenReturn(null)     // Before step 1
            .thenReturn(step1Id)  // After step 1
            .thenReturn(step2Id)  // After step 2
            .thenReturn(step3Id); // After step 3

        // When
        process.execute(processContext);

        // Then
        // Verify all 3 steps were executed
        verify(stepExecutionService, times(3)).executeStep(any(), stepCaptor.capture());

        List<ProcessStep<SecurityAnalysisConfig>> executedSteps = stepCaptor.getAllValues();
        assertEquals(3, executedSteps.size());

        // Verify no steps were skipped
        verify(stepExecutionService, never()).skipStep(any(), any());
    }

    @Test
    void executeShouldLinkStepsWithPreviousStepId() {
        // Given
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();

        when(processContext.getLastExecutedStepId())
            .thenReturn(null)     // Before step 1
            .thenReturn(step1Id)  // After step 1
            .thenReturn(step2Id); // After step 2

        // When
        process.execute(processContext);

        // Then
        verify(stepExecutionService, times(3)).executeStep(any(), stepCaptor.capture());

        List<ProcessStep<SecurityAnalysisConfig>> executedSteps = stepCaptor.getAllValues();

        // First step should have no previous step
        assertNull(executedSteps.get(0).getPreviousStepId());

        // Second step should link to first step
        assertEquals(step1Id, executedSteps.get(1).getPreviousStepId());

        // Third step should link to second step
        assertEquals(step2Id, executedSteps.get(2).getPreviousStepId());
    }

    @Test
    void executeShouldSkipAllRemainingStepsWhenFirstStepFails() {
        // Given - Make the first step fail
        doThrow(new RuntimeException("Network loading failed"))
            .when(stepExecutionService).executeStep(any(), argThat(step ->
                "LOAD_NETWORK".equals(step.getType().getName())
            ));

        // When
        process.execute(processContext);

        // Then
        // First step should be attempted
        verify(stepExecutionService).executeStep(any(), argThat(step ->
            "LOAD_NETWORK".equals(step.getType().getName())
        ));

        // Steps 2 and 3 should be skipped
        verify(stepExecutionService).skipStep(any(), argThat(step ->
            "APPLY_MODIFICATIONS".equals(step.getType().getName())
        ));

        verify(stepExecutionService).skipStep(any(), argThat(step ->
            "RUN_COMPUTATION".equals(step.getType().getName())
        ));
    }
}
