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

/**
 * Unit tests for StepExecutionService
 *
 * Tests cover:
 * - Successful step execution
 * - Step skipping
 * - Step failure handling
 * - Notification and report sending
 * - Status transitions (RUNNING -> COMPLETED/FAILED)
 * - Step context updates
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
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
    private ProcessConfig processConfig;

    @Mock
    private ReportNode reportNode;

    @Captor
    private ArgumentCaptor<ProcessExecutionStep> stepCaptor;

    @Captor
    private ArgumentCaptor<ReportInfos> reportInfosCaptor;

    private StepExecutionService<ProcessConfig> stepExecutionService;

    @BeforeEach
    void setUp() {
        stepExecutionService = new StepExecutionService<>(notificationService, reportService);
    }

    @Test
    void executeStepShouldCompleteSuccessfullyWhenNoExceptionThrown() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
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
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), stepCaptor.capture());

        ProcessExecutionStep runningStep = stepCaptor.getAllValues().get(0);
        assertEquals(StepStatus.RUNNING, runningStep.getStatus());
        assertEquals("TEST_STEP", runningStep.getStepType());
        assertEquals(previousStepId, runningStep.getPreviousStepId());

        ProcessExecutionStep completedStep = stepCaptor.getAllValues().get(1);
        assertEquals(StepStatus.COMPLETED, completedStep.getStatus());
        assertNotNull(completedStep.getCompletedAt());
    }

    @Test
    void executeStepShouldSendFailedStatusWhenExceptionThrown() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID reportUuid = UUID.randomUUID();
        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, reportUuid);

        when(processStep.getType()).thenReturn(processStepType);
        when(processStepType.getName()).thenReturn("FAILING_STEP");
        when(processStep.getPreviousStepId()).thenReturn(null);

        RuntimeException stepException = new RuntimeException("Step execution failed");
        doThrow(stepException).when(processStep).execute(context);

        // When & Then
        RuntimeException thrownException = assertThrows(
            RuntimeException.class,
            () -> stepExecutionService.executeStep(context, processStep)
        );

        assertEquals("Step execution failed", thrownException.getMessage());

        // Verify failed status was sent
        verify(notificationService, times(2)).updateStepStatus(eq(executionId), stepCaptor.capture());

        ProcessExecutionStep failedStep = stepCaptor.getAllValues().get(1);
        assertEquals(StepStatus.FAILED, failedStep.getStatus());
        assertNotNull(failedStep.getCompletedAt());

        // Verify report was NOT sent on failure
        verify(reportService, never()).sendReport(any(ReportInfos.class));
    }

    @Test
    void skipStepShouldSendSkippedStatusWithoutExecutingStep() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID previousStepId = UUID.randomUUID();
        UUID reportUuid = UUID.randomUUID();

        ProcessStepExecutionContext<ProcessConfig> context = createStepExecutionContext(executionId, reportUuid);

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
        verify(notificationService).updateStepStatus(eq(executionId), stepCaptor.capture());

        ProcessExecutionStep skippedStep = stepCaptor.getValue();
        assertEquals(StepStatus.SKIPPED, skippedStep.getStatus());
        assertEquals("SKIPPED_STEP", skippedStep.getStepType());
        assertEquals(previousStepId, skippedStep.getPreviousStepId());
        assertNull(skippedStep.getResultId());
        assertNull(skippedStep.getResultType());
        assertNull(skippedStep.getReportId());
        assertNotNull(skippedStep.getStartedAt());
        assertNotNull(skippedStep.getCompletedAt());
    }

    private ProcessStepExecutionContext<ProcessConfig> createStepExecutionContext(UUID executionId, UUID reportUuid) {
        when(processExecutionContext.getExecutionId()).thenReturn(executionId);

        ReportInfos reportInfos = new ReportInfos(reportUuid, reportNode);

        ProcessStepExecutionContext<ProcessConfig> context = mock(ProcessStepExecutionContext.class);
        when(context.getProcessContext()).thenReturn(processExecutionContext);
        when(context.getProcessExecutionId()).thenReturn(executionId);
        when(context.getStepExecutionId()).thenReturn(UUID.randomUUID());
        when(context.getStartedAt()).thenReturn(java.time.Instant.now());
        when(context.getReportInfos()).thenReturn(reportInfos);

        return context;
    }
}
