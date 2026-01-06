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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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
    void consumeexecutionstatusShouldProcessValidMessage() throws JsonProcessingException {
        // Arrange
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStatusUpdate statusUpdate = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.RUNNING)
                .executionEnvName("env-1")
                .completedAt(null)
                .build();

        String payload = objectMapper.writeValueAsString(statusUpdate);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(payload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeExecutionStatus();

        // Act
        consumer.accept(message);

        // Assert
        verify(orchestratorService).updateExecutionStatus(
                eq(executionId),
                eq(ProcessStatus.RUNNING),
                eq("env-1"),
                eq(null)
        );
    }

    @Test
    void consumeexecutionstatusShouldHandleCompletedStatus() throws JsonProcessingException {
        // Arrange
        UUID executionId = UUID.randomUUID();
        Instant completedAt = Instant.now();

        ProcessExecutionStatusUpdate statusUpdate = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.COMPLETED)
                .executionEnvName("env-prod")
                .completedAt(completedAt)
                .build();

        String payload = objectMapper.writeValueAsString(statusUpdate);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(payload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeExecutionStatus();

        // Act
        consumer.accept(message);

        // Assert
        verify(orchestratorService).updateExecutionStatus(
                eq(executionId),
                eq(ProcessStatus.COMPLETED),
                eq("env-prod"),
                eq(completedAt)
        );
    }

    @Test
    void consumeexecutionstatusShouldHandleFailedStatus() throws JsonProcessingException {
        // Arrange
        UUID executionId = UUID.randomUUID();
        Instant completedAt = Instant.now();

        ProcessExecutionStatusUpdate statusUpdate = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.FAILED)
                .executionEnvName("env-test")
                .completedAt(completedAt)
                .build();

        String payload = objectMapper.writeValueAsString(statusUpdate);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(payload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeExecutionStatus();

        // Act
        consumer.accept(message);

        // Assert
        verify(orchestratorService).updateExecutionStatus(
                eq(executionId),
                eq(ProcessStatus.FAILED),
                eq("env-test"),
                eq(completedAt)
        );
    }

    @Test
    void consumeexecutionstatusShouldHandleInvalidJson() {
        // Arrange
        UUID executionId = UUID.randomUUID();
        String invalidPayload = "{invalid json}";

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(invalidPayload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeExecutionStatus();

        // Act - Should not throw exception, just log error
        consumer.accept(message);

        // Assert - Service should not be called when JSON is invalid
        verify(orchestratorService, never()).updateExecutionStatus(any(), any(), any(), any());
    }

    @Test
    void consumeexecutionstatusShouldHandleInvalidExecutionId() throws JsonProcessingException {
        // Arrange
        ProcessExecutionStatusUpdate statusUpdate = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.RUNNING)
                .executionEnvName("env-1")
                .build();

        String payload = objectMapper.writeValueAsString(statusUpdate);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_EXECUTION_ID, "not-a-valid-uuid");

        Message<String> message = new GenericMessage<>(payload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeExecutionStatus();

        // Act & Assert - Should throw IllegalArgumentException for invalid UUID
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            consumer.accept(message);
        });

        verify(orchestratorService, never()).updateExecutionStatus(any(), any(), any(), any());
    }

    @Test
    void consumestatusShouldHandleExecutionStatusUpdateMessage() throws JsonProcessingException {
        // Arrange
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStatusUpdate statusUpdate = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.RUNNING)
                .executionEnvName("env-1")
                .build();

        String payload = objectMapper.writeValueAsString(statusUpdate);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, ConsumerService.MESSAGE_TYPE_EXECUTION_STATUS);
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(payload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeStatus();

        // Act
        consumer.accept(message);

        // Assert
        verify(orchestratorService).updateExecutionStatus(
                eq(executionId),
                eq(ProcessStatus.RUNNING),
                eq("env-1"),
                eq(null)
        );
        verify(orchestratorService, never()).updateStepStatus(any(), any());
    }

    @Test
    void consumestatusShouldHandleStepStatusUpdateMessage() throws JsonProcessingException {
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
        headers.put(ConsumerService.HEADER_MESSAGE_TYPE, ConsumerService.MESSAGE_TYPE_STEP_STATUS);
        headers.put(ConsumerService.HEADER_EXECUTION_ID, executionId.toString());

        Message<String> message = new GenericMessage<>(payload, headers);

        Consumer<Message<String>> consumer = consumerService.consumeStatus();

        // Act
        consumer.accept(message);

        // Assert
        verify(orchestratorService).updateStepStatus(eq(executionId), any(ProcessExecutionStep.class));
        verify(orchestratorService, never()).updateExecutionStatus(any(), any(), any(), any());
    }
}
