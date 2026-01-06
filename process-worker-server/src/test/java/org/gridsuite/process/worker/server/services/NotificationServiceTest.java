/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import org.gridsuite.process.commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 *
 * Tests cover:
 * - Execution status updates
 * - Step status updates
 * - Message header validation
 * - Payload validation
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private StreamBridge streamBridge;

    @Captor
    private ArgumentCaptor<Message<ProcessExecutionStatusUpdate>> executionStatusMessageCaptor;

    @Captor
    private ArgumentCaptor<Message<ProcessExecutionStep>> stepStatusMessageCaptor;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(streamBridge);
    }

    @Test
    void updateExecutionStatusShouldSendCorrectMessageWhenStatusIsRunning() {
        // Given
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStatusUpdate statusUpdate = new ProcessExecutionStatusUpdate(
            ProcessStatus.RUNNING,
            "test-env",
            null
        );

        when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

        // When
        notificationService.updateExecutionStatus(executionId, statusUpdate);

        // Then
        verify(streamBridge).send(
            eq("publishProcessUpdate-out-0"),
            executionStatusMessageCaptor.capture()
        );

        Message<ProcessExecutionStatusUpdate> sentMessage = executionStatusMessageCaptor.getValue();

        // Verify payload
        assertNotNull(sentMessage.getPayload());
        assertEquals(ProcessStatus.RUNNING, sentMessage.getPayload().getStatus());
        assertEquals("test-env", sentMessage.getPayload().getExecutionEnvName());
        assertNull(sentMessage.getPayload().getCompletedAt());

        // Verify headers
        assertEquals("EXECUTION_STATUS_UPDATE", sentMessage.getHeaders().get(NotificationService.HEADER_MESSAGE_TYPE));
        assertEquals(executionId.toString(), sentMessage.getHeaders().get(NotificationService.HEADER_EXECUTION_ID));
    }

    @Test
    void updateExecutionStatusShouldSendCorrectMessageWhenStatusIsCompleted() {
        // Given
        UUID executionId = UUID.randomUUID();
        Instant endTime = Instant.now();
        ProcessExecutionStatusUpdate statusUpdate = new ProcessExecutionStatusUpdate(
            ProcessStatus.COMPLETED,
            "prod-env",
            endTime
        );

        when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

        // When
        notificationService.updateExecutionStatus(executionId, statusUpdate);

        // Then
        verify(streamBridge).send(
            eq("publishProcessUpdate-out-0"),
            executionStatusMessageCaptor.capture()
        );

        Message<ProcessExecutionStatusUpdate> sentMessage = executionStatusMessageCaptor.getValue();

        // Verify payload includes end time
        assertEquals(ProcessStatus.COMPLETED, sentMessage.getPayload().getStatus());
        assertEquals(endTime, sentMessage.getPayload().getCompletedAt());
    }

    @Test
    void updateExecutionStatusShouldSendCorrectMessageWhenStatusIsFailed() {
        // Given
        UUID executionId = UUID.randomUUID();
        Instant endTime = Instant.now();
        ProcessExecutionStatusUpdate statusUpdate = new ProcessExecutionStatusUpdate(
            ProcessStatus.FAILED,
            "test-env",
            endTime
        );

        when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

        // When
        notificationService.updateExecutionStatus(executionId, statusUpdate);

        // Then
        verify(streamBridge).send(
            eq("publishProcessUpdate-out-0"),
            executionStatusMessageCaptor.capture()
        );

        Message<ProcessExecutionStatusUpdate> sentMessage = executionStatusMessageCaptor.getValue();

        assertEquals(ProcessStatus.FAILED, sentMessage.getPayload().getStatus());
        assertEquals(endTime, sentMessage.getPayload().getCompletedAt());
    }

    @Test
    void updateStepStatusShouldSendCorrectMessageWhenStepIsRunning() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        UUID previousStepId = UUID.randomUUID();
        Instant startTime = Instant.now();

        ProcessExecutionStep executionStep = new ProcessExecutionStep(
            stepId,
            "LOAD_NETWORK",
            previousStepId,
            StepStatus.RUNNING,
            null,
            null,
            null,
            startTime,
            null
        );

        when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

        // When
        notificationService.updateStepStatus(executionId, executionStep);

        // Then
        verify(streamBridge).send(
            eq("publishProcessUpdate-out-0"),
            stepStatusMessageCaptor.capture()
        );

        Message<ProcessExecutionStep> sentMessage = stepStatusMessageCaptor.getValue();

        // Verify payload
        assertNotNull(sentMessage.getPayload());
        assertEquals(stepId, sentMessage.getPayload().getId());
        assertEquals("LOAD_NETWORK", sentMessage.getPayload().getStepType());
        assertEquals(previousStepId, sentMessage.getPayload().getPreviousStepId());
        assertEquals(StepStatus.RUNNING, sentMessage.getPayload().getStatus());
        assertEquals(startTime, sentMessage.getPayload().getStartedAt());
        assertNull(sentMessage.getPayload().getCompletedAt());

        // Verify headers
        assertEquals("STEP_STATUS_UPDATE", sentMessage.getHeaders().get(NotificationService.HEADER_MESSAGE_TYPE));
        assertEquals(executionId.toString(), sentMessage.getHeaders().get(NotificationService.HEADER_EXECUTION_ID));
    }

    @Test
    void updateStepStatusShouldSendCorrectMessageWhenStepIsCompleted() {
        // Given
        UUID executionId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        UUID resultUuid = UUID.randomUUID();
        UUID reportUuid = UUID.randomUUID();
        Instant startTime = Instant.now().minusSeconds(60);
        Instant endTime = Instant.now();

        ProcessExecutionStep executionStep = new ProcessExecutionStep(
            stepId,
            "RUN_COMPUTATION",
            null,
            StepStatus.COMPLETED,
            resultUuid,
            ResultType.SECURITY_ANALYSIS,
            reportUuid,
            startTime,
            endTime
        );

        when(streamBridge.send(anyString(), any(Message.class))).thenReturn(true);

        // When
        notificationService.updateStepStatus(executionId, executionStep);

        // Then
        verify(streamBridge).send(
            eq("publishProcessUpdate-out-0"),
            stepStatusMessageCaptor.capture()
        );

        Message<ProcessExecutionStep> sentMessage = stepStatusMessageCaptor.getValue();

        // Verify payload with result information
        assertEquals(StepStatus.COMPLETED, sentMessage.getPayload().getStatus());
        assertEquals(resultUuid, sentMessage.getPayload().getResultId());
        assertEquals("SECURITY_ANALYSIS", sentMessage.getPayload().getResultType());
        assertEquals(reportUuid, sentMessage.getPayload().getReportId());
        assertEquals(endTime, sentMessage.getPayload().getCompletedAt());
    }

}
