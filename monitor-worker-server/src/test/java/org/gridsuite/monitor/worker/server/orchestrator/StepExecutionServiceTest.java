/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import com.powsybl.commons.report.ReportNode;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.worker.server.clients.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.context.StepWithContext;
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
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class StepExecutionServiceTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private ReportRestClient reportRestClient;

    @Mock
    private ProcessStep<ProcessConfig> processStep;

    @Mock
    private ProcessStepType processStepType;

    private StepExecutionService stepExecutionService;

    @BeforeEach
    void setUp() {
        stepExecutionService = new StepExecutionService(notificationService, reportRestClient);
    }

    @Test
    void executeStepShouldCompleteSuccessfullyWhenNoExceptionThrown() {
        UUID executionId = UUID.randomUUID();
        int stepOrder = 1;
        UUID processReportId = UUID.randomUUID();
        when(processStepType.getName()).thenReturn("TEST_STEP");
        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, processReportId, stepOrder);
        doNothing().when(processStep).execute(context);

        assertThat(context.getStartedAt()).isNull();
        stepExecutionService.executeStep(new StepWithContext<>(processStep, context));

        verify(processStep).execute(context);
        verify(reportRestClient).sendReport(any(UUID.class), any(ReportNode.class));
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));
        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.RUNNING &&
                        "TEST_STEP".equals(step.getStepType()) &&
                        step.getStartedAt() != null &&
                        stepOrder == step.getStepOrder()
        ));
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.COMPLETED &&
                        step.getCompletedAt() != null
        ));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void executeStepShouldSendFailedStatusWhenExceptionThrown() {
        UUID executionId = UUID.randomUUID();
        UUID processReportId = UUID.randomUUID();
        int stepOrder = 2;
        when(processStepType.getName()).thenReturn("FAILING_STEP");
        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, processReportId, stepOrder);
        RuntimeException stepException = new RuntimeException("Step execution failed");
        doThrow(stepException).when(processStep).execute(context);

        RuntimeException thrownException = assertThrows(
            RuntimeException.class,
            () -> stepExecutionService.executeStep(new StepWithContext<>(processStep, context))
        );
        assertEquals("Step execution failed", thrownException.getMessage());
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));
        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.RUNNING &&
                        "FAILING_STEP".equals(step.getStepType()) &&
                        step.getStartedAt() != null &&
                        stepOrder == step.getStepOrder()
        ));
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.FAILED &&
                        step.getCompletedAt() != null
        ));
        ArgumentCaptor<ProcessExecutionStep> updates = ArgumentCaptor.forClass(ProcessExecutionStep.class);
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), updates.capture());
        assertThat(updates.getAllValues().get(1).getStartedAt()).isEqualTo(updates.getAllValues().get(0).getStartedAt());

        verifyNoMoreInteractions(notificationService);

        // Verify report was sent on failure
        verify(reportRestClient).sendReport(any(UUID.class), any(ReportNode.class));
    }

    @Test
    void executeStepShouldSetStartedAtWhenExecutionBegins() {
        UUID executionId = UUID.randomUUID();
        when(processStepType.getName()).thenReturn("TEST_STEP");
        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, UUID.randomUUID(), 0);

        assertThat(context.getStartedAt()).isNull();
        Instant before = Instant.now();
        stepExecutionService.executeStep(new StepWithContext<>(processStep, context));

        assertThat(context.getStartedAt()).isBetween(before, Instant.now());
    }

    private ProcessStepExecutionContext<ProcessConfig> createStepExecutionContext(UUID executionId, UUID processReportId, int stepOrder) {
        ProcessExecutionContext<ProcessConfig> processContext = mock(ProcessExecutionContext.class);
        when(processContext.getExecutionId()).thenReturn(executionId);
        when(processContext.getReportId()).thenReturn(processReportId);
        return new ProcessStepExecutionContext<>(processContext, processStepType, stepOrder);
    }
}
