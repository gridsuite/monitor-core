/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private MonitorService monitorService;

    private ObjectMapper objectMapper;
    private ConsumerService consumerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        consumerService = new ConsumerService(monitorService, objectMapper);
    }

    @Test
    void consumeProcessExecutionStatusUpdateMessage() throws JsonProcessingException {
        UUID executionId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2025-01-01T11:59:00Z");
        Instant completedAt = Instant.parse("2025-01-01T12:00:00Z");

        ProcessExecutionStatusUpdate statusUpdate = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.RUNNING)
                .executionEnvName("env-1")
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
        String payload = objectMapper.writeValueAsString(statusUpdate);
        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.EXECUTION_STATUS_UPDATE.toString());
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());
        Message<String> message = new GenericMessage<>(payload, headers);
        Consumer<Message<String>> consumer = consumerService.consumeMonitorUpdate();

        consumer.accept(message);

        verify(monitorService).updateExecutionStatus(
                executionId,
                ProcessStatus.RUNNING,
                "env-1",
                startedAt,
                completedAt
        );
        verify(monitorService, never()).upsertStep(any(), any());
        UUID executionId = UUID.randomUUID();
        String invalidPayload = "{invalid json}";
        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.EXECUTION_STATUS_UPDATE.toString());
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());
        Message<String> message = new GenericMessage<>(invalidPayload, headers);
        Consumer<Message<String>> consumer = consumerService.consumeMonitorUpdate();

        assertThatThrownBy(() -> consumer.accept(message))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("Failed to parse payload as ProcessExecutionStatusUpdate");

        verify(monitorService, never()).updateExecutionStatus(any(), any(), any(), any(), any());
        verify(monitorService, never()).upsertStep(any(), any());
    }

    @Test
    void consumeMonitorUpdateStepsStatusesThrowsOnInvalidJson() {
        UUID executionId = UUID.randomUUID();
        String invalidPayload = "{invalid json}";
        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.STEPS_STATUSES_UPDATE.toString());
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());
        Message<String> message = new GenericMessage<>(invalidPayload, headers);
        Consumer<Message<String>> consumer = consumerService.consumeMonitorUpdate();

        assertThatThrownBy(() -> consumer.accept(message))
            .isInstanceOf(UncheckedIOException.class)
            .hasMessageContaining("Failed to parse payload as java.util.List<org.gridsuite.monitor.commons.ProcessExecutionStep>");

        verify(monitorService, never()).updateExecutionStatus(any(), any(), any(), any(), any());
        verify(monitorService, never()).upsertSteps(any(), any());
    }

    @Test
    void consumeProcessExecutionStepUpdateMessage() throws JsonProcessingException {
        UUID executionId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        ProcessExecutionStep stepUpdate = ProcessExecutionStep.builder()
                .id(stepId)
                .stepType("LOAD_FLOW")
                .status(StepStatus.RUNNING)
                .startedAt(Instant.now())
                .build();
        String payload = objectMapper.writeValueAsString(stepUpdate);
        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.STEP_STATUS_UPDATE.toString());
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());
        Message<String> message = new GenericMessage<>(payload, headers);
        Consumer<Message<String>> consumer = consumerService.consumeMonitorUpdate();

        consumer.accept(message);

        verify(monitorService).upsertStep(eq(executionId), any(ProcessExecutionStep.class));
        verify(monitorService, never()).updateExecutionStatus(any(), any(), any(), any(), any());
    }

    @Test
    void consumeProcessExecutionStepsUpdateMessage() throws JsonProcessingException {
        UUID executionId = UUID.randomUUID();
        UUID stepId1 = UUID.randomUUID();
        UUID stepId2 = UUID.randomUUID();
        ProcessExecutionStep stepUpdate1 = ProcessExecutionStep.builder()
            .id(stepId1)
            .stepType("LOAD_NETWORK")
            .status(StepStatus.SCHEDULED)
            .startedAt(Instant.now())
            .build();
        ProcessExecutionStep stepUpdate2 = ProcessExecutionStep.builder()
            .id(stepId2)
            .stepType("SECURITY_ANALYSIS")
            .status(StepStatus.SCHEDULED)
            .startedAt(Instant.now())
            .build();
        String payload = objectMapper.writeValueAsString(List.of(stepUpdate1, stepUpdate2));
        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.STEPS_STATUSES_UPDATE.toString());
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());
        Message<String> message = new GenericMessage<>(payload, headers);
        Consumer<Message<String>> consumer = consumerService.consumeMonitorUpdate();

        consumer.accept(message);

        verify(monitorService).upsertSteps(eq(executionId), any(List.class));
        verify(monitorService, never()).upsertStep(any(), any());
        verify(monitorService, never()).updateExecutionStatus(any(), any(), any(), any(), any());
    }
}
