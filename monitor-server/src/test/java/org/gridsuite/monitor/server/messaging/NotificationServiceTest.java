/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.messaging;

import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ModificationInfo;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

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
    private UUID reportId;
    private UUID loadflowParametersUuid;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        parametersUuid = UUID.randomUUID();
        executionId = UUID.randomUUID();
        reportId = UUID.randomUUID();
        loadflowParametersUuid = UUID.randomUUID();

        securityAnalysisConfig = new SecurityAnalysisConfig(
                parametersUuid,
                List.of(new ModificationInfo(UUID.randomUUID(), "descr1", true),
                    new ModificationInfo(UUID.randomUUID(), "descr2", true)),
                loadflowParametersUuid
        );
    }

    @Test
    void sendProcessRunMessage() {
        String debugFileLocation = "debug/file/location";
        notificationService.sendProcessRunMessage(caseUuid, securityAnalysisConfig, executionId, reportId, debugFileLocation);

        verify(publisher).send(
                eq("publishRunSecurityAnalysis-out-0"),
                argThat((ProcessRunMessage<?> message) ->
                        message.executionId().equals(executionId) &&
                        message.caseUuid().equals(caseUuid) &&
                        message.reportId().equals(reportId) &&
                        message.config().equals(securityAnalysisConfig) &&
                        message.debugFileLocation().equals(debugFileLocation))
        );
    }

    @Test
    void sendProcessUpdatedMessage() {
        notificationService.sendProcessUpdatedMessage(executionId, ProcessType.SECURITY_ANALYSIS);

        verify(publisher).send(
                eq("publishMonitorUpdate-out-0"),
                argThat((Message<?> message) ->
                        message.getPayload().equals("") &&
                            "PROCESS_EXECUTION_UPDATED".equals(message.getHeaders().get("updateType")) &&
                            executionId.equals(message.getHeaders().get("processExecutionId")) &&
                            ProcessType.SECURITY_ANALYSIS.name().equals(message.getHeaders().get("processType")))
        );
    }
}
