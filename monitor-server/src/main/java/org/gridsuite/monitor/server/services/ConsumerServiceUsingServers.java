/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.CaseResultInfos;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.ProcessRunMessage;
import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.StepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Configuration
public class ConsumerServiceUsingServers {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerServiceUsingServers.class);

    private final NetworkModificationRestService networkModificationRestService;
    private final SecurityAnalysisRestService securityAnalysisRestService;
    private final MonitorService monitorService;

    private record ProcessExecutionContext(
        UUID applyModificationsStepId,
        UUID securityAnalysisStepId,
        UUID caseUuid,
        List<String> contingencies,
        UUID parametersUuid
    ) { }

    private final Map<UUID, ProcessExecutionContext> processExecutionContexts = new ConcurrentHashMap<>();

    @Autowired
    ConsumerServiceUsingServers(NetworkModificationRestService networkModificationRestService,
                                SecurityAnalysisRestService securityAnalysisRestService,
                                MonitorService monitorService) {
        this.networkModificationRestService = networkModificationRestService;
        this.securityAnalysisRestService = securityAnalysisRestService;
        this.monitorService = monitorService;
    }

    @Bean
    public <T extends ProcessConfig> Consumer<Message<ProcessRunMessage<T>>> consumeRunUsingServers() {
        // consume message to launch process
        //
        return message -> {
            ProcessRunMessage<T> processRunMessage = message.getPayload();
            UUID caseUuid = processRunMessage.caseUuid();
            UUID executionId = processRunMessage.executionId();

            ProcessConfig processConfig = processRunMessage.config();
            List<UUID> modificationUuids = processConfig.modificationUuids();

            UUID applyModificationsStepId = UUID.randomUUID();
            UUID securityAnalysisStepId = UUID.randomUUID();

            List<String> contingencies = null;
            UUID parametersUuid = null;
            switch (processConfig.processType()) {
                case SECURITY_ANALYSIS -> {
                    contingencies = ((SecurityAnalysisConfig) processConfig).contingencies();
                    parametersUuid = ((SecurityAnalysisConfig) processConfig).parametersUuid();
                }
            }

            processExecutionContexts.put(executionId, new ProcessExecutionContext(
                applyModificationsStepId,
                securityAnalysisStepId,
                caseUuid,
                contingencies,
                parametersUuid
            ));

            monitorService.updateExecutionStatus(executionId, ProcessStatus.RUNNING, null, Instant.now(), null);
            monitorService.updateStepStatus(executionId,
                new ProcessExecutionStep(applyModificationsStepId, "APPLY_MODIFICATIONS", 0, StepStatus.RUNNING, null, null, null, Instant.now(), null, null));
            monitorService.updateStepStatus(executionId,
                new ProcessExecutionStep(securityAnalysisStepId, "SECURITY_ANALYSIS", 1, StepStatus.SCHEDULED, null, null, null, null, null, null));

            // call network-modification-server to apply modifications
            networkModificationRestService.applyModifications(caseUuid, executionId, modificationUuids);
        };
    }

    @Bean
    public Consumer<Message<CaseResultInfos>> consumeNetworkModifications() {
        // consume message received from network-modification-server
        //
        return message -> {
            CaseResultInfos caseResultInfos = message.getPayload();
            UUID caseResultUuid = caseResultInfos.getCaseResultUuid();
            UUID executionId = caseResultInfos.getExecutionUuid();
            String stepType = caseResultInfos.getStepType();
            UUID reportUuid = caseResultInfos.getReportUuid();

            StepStatus stepStatus = StepStatus.valueOf(caseResultInfos.getStatus());

            ProcessExecutionContext processExecutionContext = processExecutionContexts.get(executionId);
            if (processExecutionContext == null) {
                LOGGER.error("Process execution context not found for executionId: {}", executionId);
                return;
            }

            monitorService.updateStepStatus(executionId,
                new ProcessExecutionStep(processExecutionContext.applyModificationsStepId(), stepType, 0, stepStatus, null, null, reportUuid, null, Instant.now(), caseResultUuid));
            monitorService.updateStepStatus(executionId,
                new ProcessExecutionStep(processExecutionContext.securityAnalysisStepId(), "SECURITY_ANALYSIS", 1,
                    stepStatus == StepStatus.COMPLETED ? StepStatus.RUNNING : StepStatus.SKIPPED,
                    null, null, null, Instant.now(), null, null));

            if (stepStatus == StepStatus.COMPLETED) {
                // call security-analysis-server to run security analysis
                securityAnalysisRestService.runSecurityAnalysis(caseResultUuid, executionId, processExecutionContext.contingencies(), processExecutionContext.parametersUuid());
            } else {
                processExecutionContexts.remove(executionId);
            }
        };
    }

    @Bean
    public Consumer<Message<CaseResultInfos>> consumeSecurityAnalysis() {
        // consume message received from security-analysis-server
        //
        return message -> {
            CaseResultInfos caseResultInfos = message.getPayload();
            UUID executionId = caseResultInfos.getExecutionUuid();
            String stepType = caseResultInfos.getStepType();
            UUID reportUuid = caseResultInfos.getReportUuid();
            UUID resultUuid = caseResultInfos.getResultUuid();
            StepStatus stepStatus = StepStatus.valueOf(caseResultInfos.getStatus());
            UUID caseResultUuid = caseResultInfos.getCaseResultUuid();

            ProcessExecutionContext processExecutionContext = processExecutionContexts.get(executionId);
            if (processExecutionContext == null) {
                LOGGER.error("Process execution context not found for executionId: {}", executionId);
                return;
            }

            monitorService.updateStepStatus(executionId,
                new ProcessExecutionStep(processExecutionContext.securityAnalysisStepId(), stepType, 1, stepStatus, resultUuid, ResultType.SECURITY_ANALYSIS, reportUuid, null, Instant.now(), caseResultUuid));
            monitorService.updateExecutionStatus(executionId,
                stepStatus == StepStatus.COMPLETED ? ProcessStatus.COMPLETED : ProcessStatus.FAILED,
                null, null, Instant.now());

            processExecutionContexts.remove(executionId);
        };
    }
}
