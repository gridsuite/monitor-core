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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProcessExecutionService
 *
 * Tests cover:
 * - Process registration and lookup
 * - Successful process execution
 * - Process failure handling
 * - Execution status notifications (RUNNING, COMPLETED, FAILED)
 * - Execution context creation
 * - Exception propagation
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessExecutionServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Process<ProcessConfig> process1;

    @Mock
    private Process<ProcessConfig> process2;

    @Mock
    private ProcessConfig processConfig;

    @Captor
    private ArgumentCaptor<ProcessExecutionStatusUpdate> statusUpdateCaptor;

    @Captor
    private ArgumentCaptor<ProcessExecutionContext<ProcessConfig>> contextCaptor;

    private ProcessExecutionService processExecutionService;

    private static final String EXECUTION_ENV_NAME = "test-env";

    @BeforeEach
    void setUp() {
        when(process1.getProcessType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(process2.getProcessType()).thenReturn(ProcessType.SECURITY_ANALYSIS);

        List<Process<? extends ProcessConfig>> processList = List.of(process1, process2);
        processExecutionService = new ProcessExecutionService(processList, notificationService);
        ReflectionTestUtils.setField(processExecutionService, "executionEnvName", EXECUTION_ENV_NAME);
    }

    @Test
    void executeProcessShouldCompleteSuccessfullyWhenProcessExecutesWithoutError() {
        // Given
        UUID executionId = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(processConfig.executionId()).thenReturn(executionId);

        doNothing().when(process1).execute(any(ProcessExecutionContext.class));

        // When
        processExecutionService.executeProcess(processConfig);

        // Then
        verify(process1).execute(contextCaptor.capture());
        ProcessExecutionContext<ProcessConfig> context = contextCaptor.getValue();

        assertEquals(executionId, context.getExecutionId());
        assertEquals(processConfig, context.getConfig());
        assertEquals(EXECUTION_ENV_NAME, context.getExecutionEnvName());

        // Verify status notifications: RUNNING and COMPLETED
        verify(notificationService, times(2)).updateExecutionStatus(eq(executionId), statusUpdateCaptor.capture());

        List<ProcessExecutionStatusUpdate> updates = statusUpdateCaptor.getAllValues();

        // First update: RUNNING
        assertEquals(ProcessStatus.RUNNING, updates.get(0).getStatus());
        assertEquals(EXECUTION_ENV_NAME, updates.get(0).getExecutionEnvName());
        assertNull(updates.get(0).getCompletedAt());

        // Second update: COMPLETED
        assertEquals(ProcessStatus.COMPLETED, updates.get(1).getStatus());
        assertEquals(EXECUTION_ENV_NAME, updates.get(1).getExecutionEnvName());
        assertNotNull(updates.get(1).getCompletedAt());
    }

    @Test
    void executeProcessShouldSendFailedStatusWhenProcessThrowsException() {
        // Given
        UUID executionId = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(processConfig.executionId()).thenReturn(executionId);

        RuntimeException processException = new RuntimeException("Process execution failed");
        doThrow(processException).when(process1).execute(any(ProcessExecutionContext.class));

        // When
        processExecutionService.executeProcess(processConfig);

        // Then
        verify(process1).execute(any(ProcessExecutionContext.class));

        // Verify status notifications: RUNNING and FAILED
        verify(notificationService, times(2)).updateExecutionStatus(eq(executionId), statusUpdateCaptor.capture());

        List<ProcessExecutionStatusUpdate> updates = statusUpdateCaptor.getAllValues();

        // First update: RUNNING
        assertEquals(ProcessStatus.RUNNING, updates.get(0).getStatus());

        // Second update: FAILED
        assertEquals(ProcessStatus.FAILED, updates.get(1).getStatus());
        assertNotNull(updates.get(1).getCompletedAt());
    }

    @Test
    void executeProcessShouldThrowIllegalArgumentExceptionWhenProcessTypeNotFound() {
        // Given
        UUID executionId = UUID.randomUUID();
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        when(processConfig.executionId()).thenReturn(executionId);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processExecutionService.executeProcess(processConfig)
        );

        assertTrue(exception.getMessage().contains("No process found for type"));
        assertTrue(exception.getMessage().contains("SENSITIVITY_ANALYSIS"));

        // Verify no process was executed
        verify(process1, never()).execute(any());
        verify(process2, never()).execute(any());

        // Verify no notifications were sent
        verifyNoInteractions(notificationService);
    }

}
