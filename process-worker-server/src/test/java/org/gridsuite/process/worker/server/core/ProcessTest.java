/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.core;

import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.worker.server.services.NetworkConversionService;
import org.gridsuite.process.worker.server.services.NotificationService;
import org.gridsuite.process.worker.server.services.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessTest {
    @Mock
    private StepExecutionService<ProcessConfig> stepExecutionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NetworkConversionService networkConversionService;

    @Mock
    private ProcessExecutionContext<ProcessConfig> processContext;

    private TestProcess process;

    @BeforeEach
    void setUp() {
        process = new TestProcess(
                stepExecutionService,
                notificationService,
                networkConversionService);
    }

    @Test
    void executeShouldExecuteAllStepsSuccessfullyWhenNoErrors() {
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();
        UUID step3Id = UUID.randomUUID();

        ProcessStepExecutionContext<ProcessConfig> stepContext1 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext2 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext3 = mock(ProcessStepExecutionContext.class);

        when(stepContext1.getStepExecutionId()).thenReturn(step1Id);
        when(stepContext2.getStepExecutionId()).thenReturn(step2Id);
        when(stepContext3.getStepExecutionId()).thenReturn(step3Id);

        when(processContext.createStepContext(any(), any()))
                .thenReturn(stepContext1)
                .thenReturn(stepContext2)
                .thenReturn(stepContext3);

        process.execute(processContext);

        verify(stepExecutionService, times(3)).executeStep(any(), any());
        verify(stepExecutionService, never()).skipStep(any(), any());
        // Verify previousStepId is correctly set
        InOrder inOrder = inOrder(processContext);
        inOrder.verify(processContext).createStepContext(any(), eq(null));
        inOrder.verify(processContext).createStepContext(any(), eq(step1Id));
        inOrder.verify(processContext).createStepContext(any(), eq(step2Id));
    }

    @Test
    void executeShouldSkipAllRemainingStepsWhenFirstStepFails() {
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();
        UUID step3Id = UUID.randomUUID();
        ProcessStepExecutionContext<ProcessConfig> stepContext1 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext2 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext3 = mock(ProcessStepExecutionContext.class);
        when(stepContext1.getStepExecutionId()).thenReturn(step1Id);
        when(stepContext2.getStepExecutionId()).thenReturn(step2Id);
        when(stepContext3.getStepExecutionId()).thenReturn(step3Id);
        when(processContext.createStepContext(any(), any()))
                .thenReturn(stepContext1)
                .thenReturn(stepContext2)
                .thenReturn(stepContext3);
        // Make the first step fail
        doThrow(new RuntimeException("Network loading failed"))
                .when(stepExecutionService).executeStep(eq(stepContext1), any());

        process.execute(processContext);

        verify(stepExecutionService).executeStep(eq(stepContext1), any());
        verify(stepExecutionService).skipStep(eq(stepContext2), any());
        verify(stepExecutionService).skipStep(eq(stepContext3), any());
        verify(stepExecutionService, times(1)).executeStep(any(), any());
        verify(stepExecutionService, times(2)).skipStep(any(), any());
        // Verify previousStepId is correctly set
        InOrder inOrder = inOrder(processContext);
        inOrder.verify(processContext).createStepContext(any(), eq(null));
        inOrder.verify(processContext).createStepContext(any(), eq(step1Id));
        inOrder.verify(processContext).createStepContext(any(), eq(step2Id));
    }

    /**
     * Mock implementation of AbstractProcess for testing purposes
     */
    private static class TestProcess extends AbstractProcess<ProcessConfig> {

        public TestProcess(StepExecutionService<ProcessConfig> stepExecutionService, NotificationService notificationService, NetworkConversionService networkConversionService) {
            super(ProcessType.SECURITY_ANALYSIS, stepExecutionService, notificationService, networkConversionService);
        }

        @Override
        protected List<ProcessStep<ProcessConfig>> defineSteps() {
            // Return 3 dummy steps for testing
            ProcessStep<ProcessConfig> step1 = mock(ProcessStep.class);
            ProcessStep<ProcessConfig> step2 = mock(ProcessStep.class);
            ProcessStep<ProcessConfig> step3 = mock(ProcessStep.class);

            return List.of(step1, step2, step3);
        }
    }
}
