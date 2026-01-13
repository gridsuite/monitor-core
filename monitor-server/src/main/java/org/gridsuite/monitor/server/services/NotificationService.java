/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Map<ProcessType, String> BINDING_NAMES = Map.of(
        ProcessType.SECURITY_ANALYSIS, "publishRunSecurityAnalysis-out-0"
    );

    private final StreamBridge publisher;

    public void sendProcessRunMessage(ProcessConfig processConfig, UUID executionId) {
        String bindingName = BINDING_NAMES.get(processConfig.processType());
        publisher.send(bindingName, processConfig.withExecutionId(executionId));
    }
}
