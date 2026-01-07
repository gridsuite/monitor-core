
/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import org.gridsuite.process.commons.ProcessExecutionStatusUpdate;
import org.gridsuite.process.commons.ProcessStatus;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.worker.server.core.Process;
import org.gridsuite.process.worker.server.core.ProcessExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessExecutionServiceTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private Process<ProcessConfig> process;

    @Mock
    private ProcessConfig processConfig;

    private ProcessExecutionService processExecutionService;

    private static final String EXECUTION_ENV_NAME = "test-env";

    @BeforeEach
    void setUp() {
        when(process.getProcessType()).thenReturn(ProcessType.SECURITY_ANALYSIS);

        List<Process<? extends ProcessConfig>> processList = List.of(process);
        processExecutionService = new ProcessExecutionService(processList, notificationService, EXECUTION_ENV_NAME);
    }

    @Test
    void executeProcessShouldCompleteSuccessfullyWhenProcessExecutesWithoutError() {
        // Given
        UUID executionId = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(processConfig.executionId()).thenReturn(executionId);

        doNothing().when(process).execute(any(ProcessExecutionContext.class));

        // When
        processExecutionService.executeProcess(processConfig);

        // Then
        verify(process).execute(argThat(context ->
                context.getExecutionId().equals(executionId) &&
                        context.getConfig().equals(processConfig) &&
                        context.getExecutionEnvName().equals(EXECUTION_ENV_NAME)
        ));

        InOrder inOrder = inOrder(notificationService);

        // Verify RUNNING status notification
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.RUNNING &&
                        update.getExecutionEnvName().equals(EXECUTION_ENV_NAME) &&
                        update.getCompletedAt() == null
        ));

        // Verify COMPLETED status notification
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.COMPLETED &&
                        update.getExecutionEnvName().equals(EXECUTION_ENV_NAME) &&
                        update.getCompletedAt() != null
        ));

        // Verify exactly 2 calls to updateExecutionStatus
        verify(notificationService, times(2)).updateExecutionStatus(eq(executionId), any(ProcessExecutionStatusUpdate.class));
    }

    @Test
    void executeProcessShouldSendFailedStatusWhenProcessThrowsException() {
        // Given
        UUID executionId = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(processConfig.executionId()).thenReturn(executionId);

        RuntimeException processException = new RuntimeException("Process execution failed");
        doThrow(processException).when(process).execute(any(ProcessExecutionContext.class));

        // When
        assertThrows(RuntimeException.class, () -> processExecutionService.executeProcess(processConfig));

        // Then
        verify(process).execute(any(ProcessExecutionContext.class));

        InOrder inOrder = inOrder(notificationService);
        // Verify RUNNING status notification
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.RUNNING
        ));

        // Verify FAILED status notification
        inOrder.verify(notificationService).updateExecutionStatus(eq(executionId), argThat(update ->
                update.getStatus() == ProcessStatus.FAILED &&
                        update.getCompletedAt() != null
        ));

        // Verify exactly 2 calls to updateExecutionStatus
        verify(notificationService, times(2)).updateExecutionStatus(eq(executionId), any(ProcessExecutionStatusUpdate.class));
    }

    @Test
    void executeProcessShouldThrowIllegalArgumentExceptionWhenProcessTypeNotFound() {
        // Given
        when(processConfig.processType()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> processExecutionService.executeProcess(processConfig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No process found for type");

        // Verify no process was executed
        verify(process, never()).execute(any());

        // Verify no notifications were sent
        verifyNoInteractions(notificationService);
    }
}