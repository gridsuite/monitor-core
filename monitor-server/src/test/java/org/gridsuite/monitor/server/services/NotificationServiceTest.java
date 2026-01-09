/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private StreamBridge publisher;

    @InjectMocks
    private NotificationService notificationService;

    private SecurityAnalysisConfig securityAnalysisConfig;
    private UUID caseUuid;
    private UUID parametersUuid;
    private UUID executionId;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        parametersUuid = UUID.randomUUID();
        executionId = UUID.randomUUID();

        securityAnalysisConfig = new SecurityAnalysisConfig(
                caseUuid,
                null,
                parametersUuid,
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID(), UUID.randomUUID())
        );
    }

    @Test
    void sendProcessRunMessage() {
        notificationService.sendProcessRunMessage(securityAnalysisConfig, executionId);

        verify(publisher).send(
                eq(ProcessType.SECURITY_ANALYSIS.getBindingName()),
                argThat((ProcessConfig config) -> config.executionId().equals(executionId))
        );
    }
}
