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
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.orchestrator.ProcessExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ProcessExecutor processExecutor;

    private ObjectMapper objectMapper;
    private ConsumerService consumerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        consumerService = new ConsumerService(processExecutor, objectMapper);
    }

    @Test
    void consumeRunMessage() throws JsonProcessingException {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        SecurityAnalysisConfig config = new SecurityAnalysisConfig(
            UUID.randomUUID(), List.of(UUID.randomUUID()), UUID.randomUUID()
        );
        ProcessRunMessage<?> runMessage = new ProcessRunMessage<>(executionId, caseUuid, config, null);
        String payload = objectMapper.writeValueAsString(runMessage);
        Message<String> message = new GenericMessage<>(payload);
        Consumer<Message<String>> consumer = consumerService.consumeRun();

        consumer.accept(message);

        verify(processExecutor).executeProcess(any(ProcessRunMessage.class));
    }

    @Test
    void consumeRunThrowsOnInvalidJson() {
        String invalidPayload = "{invalid json}";
        Message<String> message = new GenericMessage<>(invalidPayload);
        Consumer<Message<String>> consumer = consumerService.consumeRun();

        assertThatThrownBy(() -> consumer.accept(message))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("Failed to parse ProcessRunMessage");

        verify(processExecutor, never()).executeProcess(any());
    }
}
