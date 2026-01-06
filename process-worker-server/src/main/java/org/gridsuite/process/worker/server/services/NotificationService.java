package org.gridsuite.process.worker.server.services;

import org.gridsuite.process.commons.*;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final StreamBridge updatePublisher;
    private static final String PROCESS_UPDATE_BINDING = "publishProcessUpdate-out-0";

    public static final String HEADER_MESSAGE_TYPE = "messageType";
    public static final String HEADER_EXECUTION_ID = "executionId";

    public NotificationService(StreamBridge updatePublisher) {
        this.updatePublisher = updatePublisher;
    }

    private <T> void sendProcessUpdate(
            UUID executionId,
            MessageType messageType,
            T payload
    ) {
        Message<T> message = MessageBuilder.withPayload(payload)
                .setHeader(HEADER_MESSAGE_TYPE, messageType)
                .setHeader(HEADER_EXECUTION_ID, executionId.toString())
                .build();

        updatePublisher.send(PROCESS_UPDATE_BINDING, message);
    }

    public void updateExecutionStatus(UUID executionId, ProcessExecutionStatusUpdate processExecutionStatusUpdate) {
        sendProcessUpdate(
                executionId,
                MessageType.EXECUTION_STATUS_UPDATE,
                processExecutionStatusUpdate
        );
    }

    public void updateStepStatus(UUID executionId, ProcessExecutionStep processExecutionStep) {
        sendProcessUpdate(
                executionId,
                MessageType.STEP_STATUS_UPDATE,
                processExecutionStep
        );
    }
}
