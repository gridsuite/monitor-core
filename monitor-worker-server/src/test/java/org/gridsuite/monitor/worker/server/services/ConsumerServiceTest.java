/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessRunMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
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
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        ProcessRunMessage<ProcessConfig> runMessage = new ProcessRunMessage<>(executionId, caseUuid, processConfig);
        Message<ProcessRunMessage<? extends ProcessConfig>> message = MessageBuilder.withPayload(runMessage).build();
        Consumer<Message<ProcessRunMessage<? extends ProcessConfig>>> consumer = consumerService.consumeRun();

        consumer.accept(message);

        verify(processExecutionService).executeProcess(runMessage);
    }
}
