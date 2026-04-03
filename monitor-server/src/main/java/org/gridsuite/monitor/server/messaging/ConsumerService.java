/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.orchestrator.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * Consumes self-sent {@link ProcessRunMessage} from RabbitMQ and triggers process orchestration.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Configuration
public class ConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerService.class);

    private final ProcessExecutor processExecutor;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConsumerService(ProcessExecutor processExecutor, ObjectMapper objectMapper) {
        this.processExecutor = processExecutor;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<Message<String>> consumeRun() {
        return message -> {
            try {
                ProcessRunMessage<? extends ProcessConfig> runMessage = objectMapper.readValue(
                    message.getPayload(),
                    ProcessRunMessage.class
                );
                LOGGER.info("Received process run message for execution {}", runMessage.executionId());
                processExecutor.executeProcess(runMessage);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException("Failed to parse ProcessRunMessage", e);
            }
        };
    }
}
