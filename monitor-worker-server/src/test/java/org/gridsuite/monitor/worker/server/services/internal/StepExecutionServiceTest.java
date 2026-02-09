/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.internal;

import com.powsybl.commons.report.ReportNode;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.StepStatus;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.ProcessStepType;
import org.gridsuite.monitor.worker.server.dto.ReportInfos;
import org.gridsuite.monitor.worker.server.services.external.client.ReportRestClient;
import org.gridsuite.monitor.worker.server.services.messaging.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

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

    @Mock
    private ReportNode reportNode;

    private StepExecutionService<ProcessConfig> stepExecutionService;

    @BeforeEach
    void setUp() {
        stepExecutionService = new StepExecutionService<>(notificationService, reportRestClient);
    }

    @Test
    void executeStepShouldCompleteSuccessfullyWhenNoExceptionThrown() {
        UUID executionId = UUID.randomUUID();
        int stepOrder = 1;
        UUID reportUuid = UUID.randomUUID();
        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, reportUuid, stepOrder);
        when(processStep.getType()).thenReturn(processStepType);
        when(processStepType.getName()).thenReturn("TEST_STEP");
        doNothing().when(processStep).execute(context);

        stepExecutionService.executeStep(context, processStep);

        verify(processStep).execute(context);
        verify(reportRestClient).sendReport(any(ReportInfos.class));
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));
        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.RUNNING &&
                        "TEST_STEP".equals(step.getStepType()) &&
                        stepOrder == step.getStepOrder()
        ));
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.COMPLETED &&
                        step.getCompletedAt() != null
        ));
    }

    @Test
    void executeStepShouldSendFailedStatusWhenExceptionThrown() {
        UUID executionId = UUID.randomUUID();
        UUID reportUuid = UUID.randomUUID();
        int stepOrder = 2;
        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, reportUuid, stepOrder);
        when(processStep.getType()).thenReturn(processStepType);
        when(processStepType.getName()).thenReturn("FAILING_STEP");
        RuntimeException stepException = new RuntimeException("Step execution failed");
        doThrow(stepException).when(processStep).execute(context);

        RuntimeException thrownException = assertThrows(
            RuntimeException.class,
            () -> stepExecutionService.executeStep(context, processStep)
        );
        assertEquals("Step execution failed", thrownException.getMessage());
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));
        InOrder inOrder = inOrder(notificationService);
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.RUNNING &&
                        "FAILING_STEP".equals(step.getStepType()) &&
                        stepOrder == step.getStepOrder()
        ));
        inOrder.verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.FAILED &&
                        step.getCompletedAt() != null
        ));
        // Verify report was NOT sent on failure
        verify(reportRestClient, never()).sendReport(any(ReportInfos.class));
    }

    @Test
    void skipStepShouldSendSkippedStatusWithoutExecutingStep() {
        UUID executionId = UUID.randomUUID();
        int stepOrder = 3;
        ProcessStepExecutionContext<ProcessConfig> context = createSkippedStepExecutionContext(executionId, stepOrder);
        when(processStep.getType()).thenReturn(processStepType);
        when(processStepType.getName()).thenReturn("SKIPPED_STEP");

        stepExecutionService.skipStep(context, processStep);

        verify(processStep, never()).execute(any());
        // Verify report was NOT sent on skip
        verify(reportRestClient, never()).sendReport(any(ReportInfos.class));
        verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.SKIPPED &&
                        "SKIPPED_STEP".equals(step.getStepType()) &&
                        step.getStepOrder() == 3
        ));
    }

    private ProcessStepExecutionContext<ProcessConfig> createStepExecutionContext(UUID executionId, UUID reportUuid, int stepOrder) {
        ReportInfos reportInfos = new ReportInfos(reportUuid, reportNode);

        ProcessStepExecutionContext<ProcessConfig> context = mock(ProcessStepExecutionContext.class);
        when(context.getProcessExecutionId()).thenReturn(executionId);
        when(context.getStepExecutionId()).thenReturn(UUID.randomUUID());
        when(context.getStartedAt()).thenReturn(java.time.Instant.now());
        when(context.getReportInfos()).thenReturn(reportInfos);
        when(context.getStepOrder()).thenReturn(stepOrder);

        return context;
    }

    private ProcessStepExecutionContext<ProcessConfig> createSkippedStepExecutionContext(UUID executionId, int stepOrder) {
        ProcessStepExecutionContext<ProcessConfig> context = mock(ProcessStepExecutionContext.class);
        when(context.getProcessExecutionId()).thenReturn(executionId);
        when(context.getStepExecutionId()).thenReturn(UUID.randomUUID());
        when(context.getStartedAt()).thenReturn(java.time.Instant.now());
        when(context.getStepOrder()).thenReturn(stepOrder);

        return context;
    }
}
