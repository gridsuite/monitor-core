/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.messaging;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessRunMessage;
import org.gridsuite.monitor.worker.server.services.internal.ProcessExecutionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Configuration
@RequiredArgsConstructor
public class ConsumerService {

    private final ProcessExecutionService executionService;

    @Bean
    public <T extends ProcessConfig> Consumer<Message<ProcessRunMessage<T>>> consumeRun() {
        return message -> executionService.executeProcess(message.getPayload());
    }
}
