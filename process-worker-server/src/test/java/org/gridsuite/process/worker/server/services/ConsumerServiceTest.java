/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import org.gridsuite.process.commons.ProcessConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ProcessExecutionService processExecutionService;

    @Mock
    private ProcessConfig processConfig;

    private ConsumerService consumerService;

    @BeforeEach
    void setUp() {
        consumerService = new ConsumerService(processExecutionService);
    }

    @Test
    void consumeRun() {
        Message<ProcessConfig> message = MessageBuilder.withPayload(processConfig).build();
        Consumer<Message<ProcessConfig>> consumer = consumerService.consumeRun();

        consumer.accept(message);

        verify(processExecutionService).executeProcess(processConfig);
    }
}
