/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import com.powsybl.commons.report.ReportNode;
import org.gridsuite.process.commons.ProcessExecutionStep;
import org.gridsuite.process.commons.StepStatus;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.process.worker.server.core.ProcessExecutionContext;
import org.gridsuite.process.worker.server.core.ProcessStep;
import org.gridsuite.process.worker.server.core.ProcessStepType;
import org.gridsuite.process.worker.server.dto.ReportInfos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepExecutionServiceTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private ReportService reportService;

    @Mock
    private ProcessStep<ProcessConfig> processStep;

    @Mock
    private ProcessStepType processStepType;

    @Mock
    private ProcessExecutionContext<ProcessConfig> processExecutionContext;

    @Mock
    private ReportNode reportNode;

    private StepExecutionService<ProcessConfig> stepExecutionService;

    @BeforeEach
    void setUp() {
        stepExecutionService = new StepExecutionService<>(notificationService, reportService);
    }

    @Test
    void executeStepShouldCompleteSuccessfullyWhenNoExceptionThrown() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID previousStepId = UUID.randomUUID();
        UUID reportUuid = UUID.randomUUID();

        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, reportUuid);

        when(processStep.getType()).thenReturn(processStepType);
        when(processStepType.getName()).thenReturn("TEST_STEP");
        when(processStep.getPreviousStepId()).thenReturn(previousStepId);
        doNothing().when(processStep).execute(context);

        // When
        stepExecutionService.executeStep(context, processStep);

        // Then
        // Verify step execution
        verify(processStep).execute(context);

        // Verify report was sent
        verify(reportService).sendReport(any(ReportInfos.class));

        // Verify notifications were sent (RUNNING and COMPLETED)
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));

        // Verify RUNNING step notification
        verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.RUNNING &&
                        "TEST_STEP".equals(step.getStepType()) &&
                        previousStepId.equals(step.getPreviousStepId())
        ));

        // Verify COMPLETED step notification
        verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.COMPLETED &&
                        step.getCompletedAt() != null
        ));
    }

    @Test
    void executeStepShouldSendFailedStatusWhenExceptionThrown() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID reportUuid = UUID.randomUUID();
        UUID previousStepId = UUID.randomUUID();
        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, reportUuid);

        when(processStep.getType()).thenReturn(processStepType);
        when(processStepType.getName()).thenReturn("FAILING_STEP");
        when(processStep.getPreviousStepId()).thenReturn(previousStepId);

        RuntimeException stepException = new RuntimeException("Step execution failed");
        doThrow(stepException).when(processStep).execute(context);

        // When & Then
        RuntimeException thrownException = assertThrows(
            RuntimeException.class,
            () -> stepExecutionService.executeStep(context, processStep)
        );

        assertEquals("Step execution failed", thrownException.getMessage());

        // Verify notifications were sent (RUNNING and COMPLETED)
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));

        // Verify RUNNING step notification
        verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.RUNNING &&
                        "FAILING_STEP".equals(step.getStepType()) &&
                        previousStepId.equals(step.getPreviousStepId())
        ));

        // Verify FAILED step notification
        verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.FAILED &&
                        step.getCompletedAt() != null
        ));

        // Verify report was NOT sent on failure
        verify(reportService, never()).sendReport(any(ReportInfos.class));
    }

    @Test
    void skipStepShouldSendSkippedStatusWithoutExecutingStep() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID previousStepId = UUID.randomUUID();
        UUID reportUuid = UUID.randomUUID();

        ProcessStepExecutionContext<ProcessConfig> context = createSkippedStepExecutionContext(executionId, reportUuid);

        when(processStep.getType()).thenReturn(processStepType);
        when(processStepType.getName()).thenReturn("SKIPPED_STEP");
        when(processStep.getPreviousStepId()).thenReturn(previousStepId);

        // When
        stepExecutionService.skipStep(context, processStep);

        // Then
        // Verify step was NOT executed
        verify(processStep, never()).execute(any());

        // Verify report was NOT sent
        verify(reportService, never()).sendReport(any(ReportInfos.class));

        // Verify skipped notification was sent
        verify(notificationService).updateStepStatus(eq(executionId), argThat(step ->
                step.getStatus() == StepStatus.SKIPPED &&
                        "SKIPPED_STEP".equals(step.getStepType()) &&
                        previousStepId.equals(step.getPreviousStepId())
        ));
    }

    private ProcessStepExecutionContext<ProcessConfig> createStepExecutionContext(UUID executionId, UUID reportUuid) {
        ReportInfos reportInfos = new ReportInfos(reportUuid, reportNode);

        ProcessStepExecutionContext<ProcessConfig> context = mock(ProcessStepExecutionContext.class);
        when(context.getProcessContext()).thenReturn(processExecutionContext);
        when(context.getProcessExecutionId()).thenReturn(executionId);
        when(context.getStepExecutionId()).thenReturn(UUID.randomUUID());
        when(context.getStartedAt()).thenReturn(java.time.Instant.now());
        when(context.getReportInfos()).thenReturn(reportInfos);

        return context;
    }

    private ProcessStepExecutionContext<ProcessConfig> createSkippedStepExecutionContext(UUID executionId, UUID reportUuid) {
        ProcessStepExecutionContext<ProcessConfig> context = mock(ProcessStepExecutionContext.class);
        when(context.getProcessContext()).thenReturn(processExecutionContext);
        when(context.getProcessExecutionId()).thenReturn(executionId);
        when(context.getStepExecutionId()).thenReturn(UUID.randomUUID());
        when(context.getStartedAt()).thenReturn(java.time.Instant.now());

        return context;
    }
}
