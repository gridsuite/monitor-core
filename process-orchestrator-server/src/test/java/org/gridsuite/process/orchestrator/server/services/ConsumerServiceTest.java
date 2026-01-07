package org.gridsuite.process.orchestrator.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.process.commons.*;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ProcessOrchestratorService orchestratorService;

    private ObjectMapper objectMapper;
    private ConsumerService consumerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        consumerService = new ConsumerService(orchestratorService, objectMapper);
    }

    @Test
    void consumeProcessExecutionStatusUpdateMessage() throws JsonProcessingException {
        // Arrange
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStatusUpdate statusUpdate = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.RUNNING)
                .executionEnvName("env-1")
                .completedAt(Instant.parse("2025-01-01T12:00:00Z"))
                .build();

        String payload = objectMapper.writeValueAsString(statusUpdate);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.EXECUTION_STATUS_UPDATE.toString());
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(payload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeProcessUpdate();

        // Act
        consumer.accept(message);

        // Assert
        verify(orchestratorService).updateExecutionStatus(
                eq(executionId),
                eq(ProcessStatus.RUNNING),
                eq("env-1"),
                eq(Instant.parse("2025-01-01T12:00:00Z"))
        );
        verify(orchestratorService, never()).updateStepStatus(any(), any());
    }

    @Test
    void consumeProcessUpdateThrowsOnInvalidJson() {
        // Arrange
        UUID executionId = UUID.randomUUID();
        String invalidPayload = "{invalid json}";

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.EXECUTION_STATUS_UPDATE.toString());
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(invalidPayload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeProcessUpdate();

        // Act - Should throw exception
        assertThatThrownBy(() -> consumer.accept(message))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("Failed to parse payload as ProcessExecutionStatusUpdate");

        // Assert - Service should not be called when JSON is invalid
        verify(orchestratorService, never()).updateExecutionStatus(any(), any(), any(), any());
        verify(orchestratorService, never()).updateStepStatus(any(), any());
    }

    @Test
    void consumeProcessExecutionStepUpdateMessage() throws JsonProcessingException {
        // Arrange
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

        Consumer<Message<String>> consumer = consumerService.consumeProcessUpdate();

        // Act
        consumer.accept(message);

        // Assert
        verify(orchestratorService).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));
        verify(orchestratorService, never()).updateExecutionStatus(any(), any(), any(), any());
    }
}
