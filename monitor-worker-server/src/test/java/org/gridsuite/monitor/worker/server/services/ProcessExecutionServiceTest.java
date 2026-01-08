/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.worker.server.core.Process;
import org.gridsuite.monitor.worker.server.core.ProcessExecutionContext;
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

    private ProcessExecutionService processExecutionService;

    private static final String EXECUTION_ENV_NAME = "test-env";

    @BeforeEach
    void setUp() {
        when(process.getProcessType()).thenReturn(ProcessType.SECURITY_ANALYSIS);

        List<Process<ProcessConfig>> processList = List.of(process);
        processExecutionService = new ProcessExecutionService(processList, notificationService, EXECUTION_ENV_NAME);
    }

    @Test
    void executeProcessShouldCompleteSuccessfullyWhenProcessExecutesWithoutError() {
        UUID executionId = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(processConfig.executionId()).thenReturn(executionId);
        doNothing().when(process).execute(any(ProcessExecutionContext.class));

        processExecutionService.executeProcess(processConfig);

        verify(process).execute(argThat(context ->
                context.getExecutionId().equals(executionId) &&
                        context.getConfig().equals(processConfig) &&
                        context.getExecutionEnvName().equals(EXECUTION_ENV_NAME)
        ));
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

    @Test
    void executeProcessShouldSendFailedStatusWhenProcessThrowsException() {
        UUID executionId = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(processConfig.executionId()).thenReturn(executionId);
        RuntimeException processException = new RuntimeException("Process execution failed");
        doThrow(processException).when(process).execute(any(ProcessExecutionContext.class));

        assertThrows(RuntimeException.class, () -> processExecutionService.executeProcess(processConfig));

        verify(process).execute(any(ProcessExecutionContext.class));
        verify(notificationService, times(2)).updateExecutionStatus(eq(executionId), any(ProcessExecutionStatusUpdate.class));
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

        assertThatThrownBy(() -> processExecutionService.executeProcess(processConfig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No process found for type");
        verify(process, never()).execute(any());
        verifyNoInteractions(notificationService);
    }
}
