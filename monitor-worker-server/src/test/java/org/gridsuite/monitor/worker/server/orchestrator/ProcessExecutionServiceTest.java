/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import com.powsybl.commons.report.ReportNode;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.worker.server.clients.MonitorRestClient;
import org.gridsuite.monitor.worker.server.clients.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.exceptions.ProcessCancelledException;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.gridsuite.monitor.worker.server.core.process.Process;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.gridsuite.monitor.worker.server.core.process.ProcessStepType;
import org.gridsuite.monitor.worker.server.messaging.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessExecutionServiceTest {
    @Mock
    private StepExecutor stepExecutor;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Process<ProcessConfig> process;

    @Mock
    private ProcessConfig processConfig;

    @Mock
    private ReportRestClient reportRestClient;

    @Mock
    private MonitorRestClient monitorRestClient;

    private ProcessExecutionService processExecutionService;
    private TaskManager taskManager;

    private static final String EXECUTION_ENV_NAME = "test-env";

    @BeforeEach
    void setUp() {
        when(process.getProcessType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        taskManager = new TaskManager();
        processExecutionService = new ProcessExecutionService(List.of(process), stepExecutor, notificationService, reportRestClient, monitorRestClient, taskManager, EXECUTION_ENV_NAME);
    }

    private static ProcessStep<ProcessConfig> mockStep(String typeName) {
        ProcessStep<ProcessConfig> step = mock(ProcessStep.class);
        ProcessStepType type = mock(ProcessStepType.class);
        when(step.getType()).thenReturn(type);
        when(type.getName()).thenReturn(typeName);
        return step;
    }

    @Test
    void executeProcessShouldCompleteSuccessfullyWhenAllStepsSucceed() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        ProcessStep<ProcessConfig> step1 = mockStep("STEP_1");
        ProcessStep<ProcessConfig> step2 = mockStep("STEP_2");
        ProcessStep<ProcessConfig> step3 = mockStep("STEP_3");
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step1, step2, step3));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig, reportId, null);

        processExecutionService.executeProcess(runMessage);

        verify(reportRestClient, times(1)).sendReport(any(UUID.class), any(ReportNode.class));
        verify(monitorRestClient, times(1)).isExecutionCanceled(any(UUID.class));
        verifyNoMoreInteractions(monitorRestClient);
        verify(stepExecutor).executeStep(argThat(stepWithContext -> stepWithContext.step() == step1));
        verify(stepExecutor).executeStep(argThat(stepWithContext -> stepWithContext.step() == step2));
        verify(stepExecutor).executeStep(argThat(stepWithContext -> stepWithContext.step() == step3));

        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(update ->
            update.getStatus() == ProcessStatus.RUNNING &&
            update.getExecutionEnvName().equals(EXECUTION_ENV_NAME) &&
            update.getCompletedAt() == null
        ));
        inOrder.verify(notificationService).updateStepsStatuses(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(steps ->
                steps.size() == 3 &&
                        steps.get(0).getStatus() == StepStatus.SCHEDULED &&
                        steps.get(0).getId() != null &&
                        steps.get(0).getStepType().equals("STEP_1") &&
                        steps.get(0).getStepOrder() == 0 &&
                        steps.get(1).getStatus() == StepStatus.SCHEDULED &&
                        steps.get(1).getId() != null &&
                        steps.get(1).getStepType().equals("STEP_2") &&
                        steps.get(1).getStepOrder() == 1 &&
                        steps.get(2).getStatus() == StepStatus.SCHEDULED &&
                        steps.get(2).getId() != null &&
                        steps.get(2).getStepType().equals("STEP_3") &&
                        steps.get(2).getStepOrder() == 2
        ));
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(update ->
            update.getStatus() == ProcessStatus.COMPLETED &&
            update.getExecutionEnvName().equals(EXECUTION_ENV_NAME) &&
            update.getCompletedAt() != null
        ));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void executeProcessShouldSkipRemainingStepsAndSendFailedStatusWhenFirstStepFails() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        ProcessStep<ProcessConfig> step1 = mockStep("STEP_1");
        ProcessStep<ProcessConfig> step2 = mockStep("STEP_2");
        ProcessStep<ProcessConfig> step3 = mockStep("STEP_3");
        RuntimeException stepException = new RuntimeException("Step execution failed");
        doThrow(stepException).when(stepExecutor).executeStep(argThat(stepWithContext -> stepWithContext.step() == step1));
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step1, step2, step3));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig, reportId, null);

        assertThrows(RuntimeException.class,
                () -> processExecutionService.executeProcess(runMessage));

        verify(reportRestClient, times(1)).sendReport(any(UUID.class), any(ReportNode.class));
        verify(stepExecutor).executeStep(argThat(stepWithContext -> stepWithContext.step() == step1));
        verify(stepExecutor, never()).executeStep(argThat(stepWithContext -> stepWithContext.step() == step2));
        verify(stepExecutor, never()).executeStep(argThat(stepWithContext -> stepWithContext.step() == step3));

        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(update ->
            update.getStatus() == ProcessStatus.RUNNING
        ));
        inOrder.verify(notificationService).updateStepsStatuses(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(steps ->
                steps.size() == 3 &&
                        steps.get(0).getStatus() == StepStatus.SCHEDULED &&
                        steps.get(0).getStepType().equals("STEP_1") &&
                        steps.get(0).getStepOrder() == 0 &&
                        steps.get(1).getStatus() == StepStatus.SCHEDULED &&
                        steps.get(1).getStepType().equals("STEP_2") &&
                        steps.get(1).getStepOrder() == 1 &&
                        steps.get(2).getStatus() == StepStatus.SCHEDULED &&
                        steps.get(2).getStepType().equals("STEP_3") &&
                        steps.get(2).getStepOrder() == 2
        ));
        inOrder.verify(notificationService).updateStepsStatuses(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(steps ->
                steps.size() == 2 &&
                        steps.get(0).getStatus() == StepStatus.SKIPPED &&
                        steps.get(0).getStepType().equals("STEP_2") &&
                        steps.get(0).getStepOrder() == 1 &&
                        steps.get(1).getStatus() == StepStatus.SKIPPED &&
                        steps.get(1).getStepType().equals("STEP_3") &&
                        steps.get(1).getStepOrder() == 2
        ));
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(update ->
            update.getStatus() == ProcessStatus.FAILED &&
            update.getCompletedAt() != null
        ));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void repeatedRunsOnTheSameWorkerShouldHaveDifferentStepIds() {
        UUID firstExecutionId = UUID.randomUUID();
        UUID secondExecutionId = UUID.randomUUID();
        ProcessStep<ProcessConfig> step = mockStep("STEP");
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step));

        processExecutionService.executeProcess(new ProcessRunMessage<>(
                firstExecutionId, UUID.randomUUID(), processConfig, UUID.randomUUID(), null));
        processExecutionService.executeProcess(new ProcessRunMessage<>(
                secondExecutionId, UUID.randomUUID(), processConfig, UUID.randomUUID(), null));

        ArgumentCaptor<List<ProcessExecutionStep>> firstUpdates = ArgumentCaptor.captor();
        ArgumentCaptor<List<ProcessExecutionStep>> secondUpdates = ArgumentCaptor.captor();
        verify(notificationService).updateStepsStatuses(eq(firstExecutionId), eq(ProcessType.SECURITY_ANALYSIS), firstUpdates.capture());
        verify(notificationService).updateStepsStatuses(eq(secondExecutionId), eq(ProcessType.SECURITY_ANALYSIS), secondUpdates.capture());
        UUID firstStepId = firstUpdates.getValue().getFirst().getId();
        UUID secondStepId = secondUpdates.getValue().getFirst().getId();
        assertThat(firstStepId).isNotNull().isNotEqualTo(secondStepId);
    }

    @Test
    void executeProcessShouldThrowIllegalArgumentExceptionWhenProcessTypeNotFound() {
        when(processConfig.processType()).thenReturn(null);
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(UUID.randomUUID(), UUID.randomUUID(), processConfig, UUID.randomUUID(), null);

        assertThatThrownBy(() -> processExecutionService.executeProcess(runMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No process found for type");
        verifyNoInteractions(reportRestClient);
        verifyNoInteractions(notificationService);
    }

    @Test
    void executeProcessShouldSkipWhenExecutionCanceled() {
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(monitorRestClient.isExecutionCanceled(any(UUID.class))).thenReturn(true);
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(UUID.randomUUID(), UUID.randomUUID(), processConfig, UUID.randomUUID(), null);

        processExecutionService.executeProcess(runMessage);
        verify(monitorRestClient, times(1)).isExecutionCanceled(any(UUID.class));
        verifyNoInteractions(reportRestClient);
        verifyNoInteractions(notificationService);
    }

    @Test
    void executeProcessShouldStopAfterCancellationBetweenSteps() {
        UUID executionId = UUID.randomUUID();
        ProcessStep<ProcessConfig> step1 = mockStep("STEP_1");
        ProcessStep<ProcessConfig> step2 = mockStep("STEP_2");
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step1, step2));
        doAnswer(invocation -> {
            taskManager.cancelTask(executionId);
            return null;
        }).when(stepExecutor).executeStep(argThat(stepWithContext -> stepWithContext.step() == step1));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, UUID.randomUUID(), processConfig, UUID.randomUUID(), null);

        assertDoesNotThrow(() -> processExecutionService.executeProcess(runMessage));

        verify(stepExecutor).executeStep(argThat(stepWithContext -> stepWithContext.step() == step1));
        verify(stepExecutor, never()).executeStep(argThat(stepWithContext -> stepWithContext.step() == step2));
        verify(notificationService).updateExecutionStatus(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(update ->
            update.getStatus() == ProcessStatus.CANCELED && update.getCompletedAt() != null));
    }

    @Test
    void executeProcessShouldRemainCanceledWhenCancellationArrivesAfterLastStep() {
        UUID executionId = UUID.randomUUID();
        ProcessStep<ProcessConfig> step = mockStep("STEP");
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step));
        doAnswer(invocation -> {
            taskManager.cancelTask(executionId);
            return null;
        }).when(stepExecutor).executeStep(any());
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, UUID.randomUUID(), processConfig, UUID.randomUUID(), null);

        assertDoesNotThrow(() -> processExecutionService.executeProcess(runMessage));

        verify(notificationService).updateExecutionStatus(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(update ->
            update.getStatus() == ProcessStatus.CANCELED && update.getCompletedAt() != null));
        verify(notificationService, never()).updateExecutionStatus(eq(executionId), any(), argThat(update ->
            update.getStatus() == ProcessStatus.COMPLETED));
    }

    @Test
    void executeProcessShouldSendCancelledStatusWhenStepThrowsProcessCancelledException() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        ProcessStep<ProcessConfig> step1 = mockStep("STEP_1");

        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step1));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig, reportId, null);

        doThrow(new ProcessCancelledException("Execution canceled"))
                .when(stepExecutor).executeStep(any());

        assertDoesNotThrow(() -> processExecutionService.executeProcess(runMessage));

        verify(notificationService).updateExecutionStatus(eq(executionId), eq(ProcessType.SECURITY_ANALYSIS), argThat(update ->
                update.getStatus() == ProcessStatus.CANCELED &&
                        update.getCompletedAt() != null
        ));
    }
}
