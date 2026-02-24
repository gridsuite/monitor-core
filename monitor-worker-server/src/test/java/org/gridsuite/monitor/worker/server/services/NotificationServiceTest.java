/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.api.types.message.MessageType;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStep;
import org.gridsuite.monitor.worker.server.messaging.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private StreamBridge streamBridge;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(streamBridge);
    }

    @Test
    void updateExecutionStatusShouldSendExecutionStatusUpdateMessage() {
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStatusUpdate payload = new ProcessExecutionStatusUpdate();

        notificationService.updateExecutionStatus(executionId, payload);

        verify(streamBridge).send(
                eq("publishMonitorUpdate-out-0"),
                argThat((Message<?> message) -> {
                    assertThat(message.getPayload()).isSameAs(payload);
                    assertThat(message.getHeaders()).containsEntry(NotificationService.HEADER_MESSAGE_TYPE, MessageType.EXECUTION_STATUS_UPDATE);
                    assertThat(message.getHeaders()).containsEntry(NotificationService.HEADER_EXECUTION_ID, executionId.toString());
                    return true;
                })
        );
    }

    @Test
    void updateStepStatusShouldSendStepStatusUpdateMessage() {
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStep payload = new ProcessExecutionStep();

        notificationService.updateStepStatus(executionId, payload);

        verify(streamBridge).send(
                eq("publishMonitorUpdate-out-0"),
                argThat((Message<?> message) -> {
                    assertThat(message.getPayload()).isSameAs(payload);
                    assertThat(message.getHeaders()).containsEntry(NotificationService.HEADER_MESSAGE_TYPE, MessageType.STEP_STATUS_UPDATE);
                    assertThat(message.getHeaders()).containsEntry(NotificationService.HEADER_EXECUTION_ID, executionId.toString());
                    return true;
                })
        );
    }

    @Test
    void updateStepsStatusesShouldSendStepStatusUpdateMessage() {
        UUID executionId = UUID.randomUUID();
        List<ProcessExecutionStep> payload = List.of(new ProcessExecutionStep(), new ProcessExecutionStep());

        notificationService.updateStepsStatuses(executionId, payload);

        verify(streamBridge).send(
            eq("publishMonitorUpdate-out-0"),
            argThat((Message<?> message) -> {
                assertThat(message.getPayload()).isSameAs(payload);
                assertThat(message.getHeaders()).containsEntry(NotificationService.HEADER_MESSAGE_TYPE, MessageType.STEPS_STATUSES_UPDATE);
                assertThat(message.getHeaders()).containsEntry(NotificationService.HEADER_EXECUTION_ID, executionId.toString());
                return true;
            })
        );
    }
}
