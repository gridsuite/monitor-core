/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.messaging;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Publishes a {@link ProcessRunMessage} to a self-consumed RabbitMQ queue
 * for asynchronous process orchestration.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final StreamBridge publisher;

    public void sendProcessRunMessage(UUID caseUuid, ProcessConfig processConfig, UUID executionId, String debugFileLocation) {
        ProcessRunMessage<?> message = new ProcessRunMessage<>(executionId, caseUuid, processConfig, debugFileLocation);
        publisher.send("publishRun-out-0", message);
    }
}
