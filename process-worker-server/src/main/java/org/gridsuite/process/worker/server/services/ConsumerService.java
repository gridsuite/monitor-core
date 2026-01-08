/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.process.commons.ProcessConfig;
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
    public Consumer<Message<ProcessConfig>> consumeRun() {
        return message -> executionService.executeProcess(message.getPayload());
    }
}
