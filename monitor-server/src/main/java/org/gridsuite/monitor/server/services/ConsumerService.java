/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.MessageType;
import org.gridsuite.monitor.commons.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Configuration
public class ConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerService.class);

    public static final String HEADER_MESSAGE_TYPE = "messageType";
    public static final String HEADER_EXECUTION_ID = "executionId";

    private final MonitorService monitorService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConsumerService(MonitorService monitorService, ObjectMapper objectMapper) {
        this.monitorService = monitorService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> consumeMonitorUpdate() {
        return message -> {
            String messageTypeStr = message.getHeaders().get(HEADER_MESSAGE_TYPE, String.class);
            MessageType messageType = MessageType.valueOf(messageTypeStr);
            String executionIdStr = message.getHeaders().get(HEADER_EXECUTION_ID, String.class);
            UUID executionId = UUID.fromString(executionIdStr);

            switch (messageType) {
                case EXECUTION_UPDATE -> handleExecutionUpdate(executionId, message);
                case STEP_UPSERT -> handleStepUpsert(executionId, message);
                case STEPS_UPSERT -> handleStepsUpsert(executionId, message);
                default -> LOGGER.warn("Unknown message type: {}", messageType);
            }
        };
    }

    private void handleExecutionUpdate(UUID executionId, Message<String> message) {
        ProcessExecutionStatusUpdate payload = parsePayload(message.getPayload(), ProcessExecutionStatusUpdate.class);
        monitorService.updateExecution(executionId, payload.getStatus(), payload.getExecutionEnvName(), payload.getStartedAt(), payload.getCompletedAt());
    }

    private void handleStepUpsert(UUID executionId, Message<String> message) {
        ProcessExecutionStep processExecutionStep = parsePayload(message.getPayload(), ProcessExecutionStep.class);
        monitorService.upsertStep(executionId, processExecutionStep);
    }

    private void handleStepsUpsert(UUID executionId, Message<String> message) {
        List<ProcessExecutionStep> processExecutionSteps = parsePayload(message.getPayload(), new TypeReference<List<ProcessExecutionStep>>() { });
        monitorService.upsertSteps(executionId, processExecutionSteps);
    }

    private <T> T parsePayload(String payload, Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to parse payload as " + clazz.getSimpleName(), e);
        }
    }

    private <T> T parsePayload(String payload, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(payload, typeReference);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to parse payload as " + typeReference.getType().getTypeName(), e);
        }
    }
}
