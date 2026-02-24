/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.api.types.message.ProcessRunMessage;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.api.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.api.types.processexecution.StepStatus;
import org.gridsuite.monitor.worker.server.client.NetworkModificationRestClient;
import org.gridsuite.monitor.worker.server.client.ReportRestClient;
import org.gridsuite.monitor.worker.server.client.SecurityAnalysisRestClient;
import org.gridsuite.monitor.worker.server.core.process.Process;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.gridsuite.monitor.worker.server.messaging.NotificationService;
import org.gridsuite.monitor.worker.server.processes.commons.steps.ApplyModificationsStep;
import org.gridsuite.monitor.worker.server.processes.commons.steps.LoadNetworkStep;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.steps.SecurityAnalysisRunComputationStep;
import org.gridsuite.monitor.worker.server.processes.orchestrator.ProcessExecutionService;
import org.gridsuite.monitor.worker.server.processes.orchestrator.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessExecutionServiceTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private Process<ProcessConfig> process;

    @Mock
    private ProcessConfig processConfig;

    @Mock
    private NetworkConversionService networkConversionService;

    @Mock
    private NetworkModificationService networkModificationService;

    @Mock
    private NetworkModificationRestClient networkModificationRestClient;

    @Mock
    private FilterService filterService;

    @Mock
    ReportRestClient reportRestClient;

    @Mock
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    private ProcessExecutionService processExecutionService;

    private LoadNetworkStep<ProcessConfig> loadNetworkStep;

    private ApplyModificationsStep<SecurityAnalysisConfig> applyModificationsStep;

    private SecurityAnalysisRunComputationStep runComputationStep;

    private static final String EXECUTION_ENV_NAME = "test-env";

    @BeforeEach
    void setUp() {
        when(process.getProcessType()).thenReturn(ProcessType.SECURITY_ANALYSIS);

        List<Process<? extends ProcessConfig>> processList = List.of(process);
        StepExecutor stepExecutor = new StepExecutionService(notificationService, reportRestClient);
        processExecutionService = new ProcessExecutionService(processList, stepExecutor, notificationService, EXECUTION_ENV_NAME);

        loadNetworkStep = new LoadNetworkStep<>(networkConversionService);
        applyModificationsStep = new ApplyModificationsStep<>(networkModificationService, networkModificationRestClient, filterService);
        runComputationStep = new SecurityAnalysisRunComputationStep(securityAnalysisRestClient);
    }

    @Test
    void executeProcessShouldCompleteSuccessfullyWhenProcessExecutesWithoutError() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        // doNothing().when(processExecutionService).executeSteps(process, any(ProcessExecutionContext.class));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig);
        when(process.defineSteps()).thenReturn((List) List.of(loadNetworkStep, applyModificationsStep, runComputationStep));

        processExecutionService.executeProcess(runMessage);

        verify(process, times(2)).defineSteps();
        verify(notificationService, times(1)).updateStepsStatuses(eq(executionId), argThat(steps ->
            steps.size() == 3 &&
            steps.get(0).getStatus() == StepStatus.SCHEDULED &&
            steps.get(0).getId().equals(loadNetworkStep.getId()) &&
            steps.get(0).getStepType().equals(loadNetworkStep.getType().getName()) &&
            steps.get(0).getStepOrder() == 0 &&
            steps.get(1).getStatus() == StepStatus.SCHEDULED &&
            steps.get(1).getId().equals(applyModificationsStep.getId()) &&
            steps.get(1).getStepType().equals(applyModificationsStep.getType().getName()) &&
            steps.get(1).getStepOrder() == 1 &&
            steps.get(2).getStatus() == StepStatus.SCHEDULED &&
            steps.get(2).getId().equals(runComputationStep.getId()) &&
            steps.get(2).getStepType().equals(runComputationStep.getType().getName()) &&
            steps.get(2).getStepOrder() == 2
        ));

        /*
        verify(processExecutionService).executeSteps(eq(process), argThat(context ->
                context.getExecutionId().equals(executionId) &&
                        context.getCaseUuid().equals(caseUuid) &&
                        context.getConfig().equals(processConfig) &&
                        context.getExecutionEnvName().equals(EXECUTION_ENV_NAME)
        ));*/
        verify(notificationService, times(2)).updateExecutionStatus(eq(executionId), any(ProcessExecutionStatusUpdate.class));
        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.RUNNING &&
                        update.getExecutionEnvName().equals(EXECUTION_ENV_NAME) &&
                        update.getCompletedAt() == null
        ));
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.COMPLETED &&
                        update.getExecutionEnvName().equals(EXECUTION_ENV_NAME) &&
                        update.getCompletedAt() != null
        ));
    }

    @Disabled
    @Test
    void executeProcessShouldSendFailedStatusWhenProcessThrowsException() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        RuntimeException processException = new RuntimeException("Process execution failed");
        //doThrow(processException).when(processExecutionService).executeSteps(eq(process), any(ProcessExecutionContext.class));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig);

        assertThrows(RuntimeException.class, () -> processExecutionService.executeProcess(runMessage));

        /*verify(processExecutionService).executeSteps(eq(process), any(ProcessExecutionContext.class));*/
        /*verify(notificationService, times(2)).updateExecutionStatus(eq(executionId), any(ProcessExecutionStatusUpdate.class));*/
        /*
        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.RUNNING
        ));
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.FAILED &&
                        update.getCompletedAt() != null
        ));*/
    }

    @Test
    void executeProcessShouldThrowIllegalArgumentExceptionWhenProcessTypeNotFound() {
        when(processConfig.processType()).thenReturn(null);
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(UUID.randomUUID(), UUID.randomUUID(), processConfig);

        assertThatThrownBy(() -> processExecutionService.executeProcess(runMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No process found for type");
        // verify(processExecutionService, never()).executeSteps(process, any());
        verifyNoInteractions(notificationService);
    }
}
