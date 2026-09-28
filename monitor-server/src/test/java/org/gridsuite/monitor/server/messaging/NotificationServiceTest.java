/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.messaging;

import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ModificationInfo;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
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

import static org.gridsuite.monitor.server.messaging.NotificationService.*;
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
    private UUID stepId;
    private ProcessExecutionStep step;

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

        stepId = UUID.randomUUID();
        step = ProcessExecutionStep.builder().id(stepId)
                .stepType("LOAD")
                .status(StepStatus.RUNNING)
                .build();
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
                eq(PUBLISH_MONITOR_UPDATE_OUT_0),
                argThat((Message<?> message) ->
                        message.getPayload().equals("") &&
                            "PROCESS_EXECUTION_UPDATED".equals(message.getHeaders().get(UPDATE_TYPE)) &&
                            executionId.equals(message.getHeaders().get(PROCESS_EXECUTION_ID)) &&
                            ProcessType.SECURITY_ANALYSIS.name().equals(message.getHeaders().get("processType")))
        );
    }

    @Test
    void sendProcesStepUpdatedMessage() {
        notificationService.sendProcesStepUpdatedMessage(executionId, step);
        verify(publisher).send(
                eq(PUBLISH_MONITOR_UPDATE_OUT_0),
                argThat((Message<?> message) ->
                        message.getPayload().equals("") &&
                                "PROCESS_STEP_UPDATED".equals(message.getHeaders().get(UPDATE_TYPE)) &&
                                executionId.equals(message.getHeaders().get(PROCESS_EXECUTION_ID)) &&
                                stepId.equals(message.getHeaders().get("stepId")) &&
                                "LOAD".equals(message.getHeaders().get("stepType")) &&
                                StepStatus.RUNNING.equals(message.getHeaders().get("stepStatus")))
        );
    }

    @Test
    void sendProcessStepsUpdatedMessage() {
        var steps = List.of(stepId);
        notificationService.sendProcessStepsUpdatedMessage(executionId, steps);

        verify(publisher).send(
                eq(PUBLISH_MONITOR_UPDATE_OUT_0),
                argThat((Message<?> message) ->
                        message.getPayload().equals("") &&
                                "PROCESS_STEPS_UPDATED".equals(message.getHeaders().get(UPDATE_TYPE)) &&
                                executionId.equals(message.getHeaders().get(PROCESS_EXECUTION_ID)) &&
                                steps.equals(message.getHeaders().get("stepsIds")))
        );
    }
}
