/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.worker.server.clients.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.gridsuite.monitor.worker.server.core.process.Process;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.gridsuite.monitor.worker.server.core.process.ProcessStepType;
import org.gridsuite.monitor.worker.server.messaging.NotificationService;
import org.junit.jupiter.api.BeforeEach;
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
    private ReportRestClient reportRestClient;

    private ProcessExecutionService processExecutionService;

    private static final String EXECUTION_ENV_NAME = "test-env";

    @BeforeEach
    void setUp() {
        when(process.getProcessType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        StepExecutor stepExecutor = new StepExecutionService(notificationService, reportRestClient);
        processExecutionService = new ProcessExecutionService(List.of(process), stepExecutor, notificationService, EXECUTION_ENV_NAME);
    }

    private static ProcessStep<ProcessConfig> mockStep(UUID id, String typeName) {
        ProcessStep<ProcessConfig> step = mock(ProcessStep.class);
        ProcessStepType type = mock(ProcessStepType.class);
        when(step.getId()).thenReturn(id);
        when(step.getType()).thenReturn(type);
        when(type.getName()).thenReturn(typeName);
        return step;
    }

    @Test
    void executeProcessShouldCompleteSuccessfullyWhenAllStepsSucceed() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();
        UUID step3Id = UUID.randomUUID();
        ProcessStep<ProcessConfig> step1 = mockStep(step1Id, "STEP_1");
        ProcessStep<ProcessConfig> step2 = mockStep(step2Id, "STEP_2");
        ProcessStep<ProcessConfig> step3 = mockStep(step3Id, "STEP_3");
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step1, step2, step3));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig, null);

        processExecutionService.executeProcess(runMessage);

        verify(notificationService).updateStepsStatuses(eq(executionId), argThat(steps ->
            steps.size() == 3 &&
            steps.get(0).getStatus() == StepStatus.SCHEDULED &&
            steps.get(0).getId().equals(step1Id) &&
            steps.get(0).getStepType().equals("STEP_1") &&
            steps.get(0).getStepOrder() == 0 &&
            steps.get(1).getStatus() == StepStatus.SCHEDULED &&
            steps.get(1).getId().equals(step2Id) &&
            steps.get(1).getStepType().equals("STEP_2") &&
            steps.get(1).getStepOrder() == 1 &&
            steps.get(2).getStatus() == StepStatus.SCHEDULED &&
            steps.get(2).getId().equals(step3Id) &&
            steps.get(2).getStepType().equals("STEP_3") &&
            steps.get(2).getStepOrder() == 2
        ));
        verify(step1).execute(any());
        verify(step2).execute(any());
        verify(step3).execute(any());
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

    @Test
    void executeProcessShouldSkipRemainingStepsAndSendFailedStatusWhenFirstStepFails() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        ProcessStep<ProcessConfig> step1 = mockStep(UUID.randomUUID(), "STEP_1");
        ProcessStep<ProcessConfig> step2 = mockStep(UUID.randomUUID(), "STEP_2");
        ProcessStep<ProcessConfig> step3 = mockStep(UUID.randomUUID(), "STEP_3");
        RuntimeException stepException = new RuntimeException("Step execution failed");
        doThrow(stepException).when(step1).execute(any());
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process.getSteps()).thenReturn(List.of(step1, step2, step3));
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig, null);

        assertThrows(RuntimeException.class, () -> processExecutionService.executeProcess(runMessage));

        verify(step1).execute(any());
        verify(step2, never()).execute(any());
        verify(step3, never()).execute(any());
        verify(process).onStepFailure(any(ProcessExecutionContext.class), eq(step1), eq(stepException));
        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
            update.getStatus() == ProcessStatus.RUNNING
        ));
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
            update.getStatus() == ProcessStatus.FAILED &&
            update.getCompletedAt() != null
        ));
    }

    @Test
    void executeProcessShouldThrowIllegalArgumentExceptionWhenProcessTypeNotFound() {
        when(processConfig.processType()).thenReturn(null);
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(UUID.randomUUID(), UUID.randomUUID(), processConfig, null);

        assertThatThrownBy(() -> processExecutionService.executeProcess(runMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No process found for type");
        verifyNoInteractions(notificationService);
    }
}
