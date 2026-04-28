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
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final StreamBridge publisher;

    public void sendProcessRunMessage(UUID caseUuid, ProcessConfig processConfig, UUID executionId, UUID reportId, String debugFileLocation) {
        String bindingName = switch (processConfig.processType()) {
            case SECURITY_ANALYSIS -> "publishRunSecurityAnalysis-out-0";
            case LOADFLOW -> "publishRunLoadFlow-out-0";
        };
        ProcessRunMessage<?> message = new ProcessRunMessage<>(executionId, caseUuid, processConfig, reportId, debugFileLocation);
        publisher.send(bindingName, message);
    }

    public void sendProcessUpdatedMessage(ProcessType processType, UUID executionId) {
        String bindingName = "publishMonitorUpdateFront-out-0";
        Message<?> message = MessageBuilder.withPayload("")
            .setHeader("updateType", "PROCESS_EXECUTION_UPDATED")
            .setHeader("processType", processType.name())
            .setHeader("processExecutionId", executionId).build();
        publisher.send(bindingName, message);
    }
}
