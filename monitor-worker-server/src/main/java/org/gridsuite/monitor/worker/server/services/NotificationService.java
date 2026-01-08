/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.MessageType;
import org.gridsuite.monitor.commons.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
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
