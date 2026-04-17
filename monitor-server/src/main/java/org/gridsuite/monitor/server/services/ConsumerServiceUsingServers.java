/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.CaseResultInfos;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.services.processexecution.ProcessExecutionService;
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
    private final ProcessExecutionService processExecutionService;

    private record ProcessExecutionContext(
        UUID applyModificationsStepId,
        UUID securityAnalysisStepId,
        UUID caseUuid,
        UUID securityAnalysisParametersUuid,
        UUID loadflowParametersUuid
    ) { }

    private final Map<UUID, ProcessExecutionContext> processExecutionContexts = new ConcurrentHashMap<>();

    @Autowired
    ConsumerServiceUsingServers(NetworkModificationRestService networkModificationRestService,
                                SecurityAnalysisRestService securityAnalysisRestService,
                                ProcessExecutionService processExecutionService) {
        this.networkModificationRestService = networkModificationRestService;
        this.securityAnalysisRestService = securityAnalysisRestService;
        this.processExecutionService = processExecutionService;
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

            UUID securityAnalysisParametersUuid = null;
            UUID loadflowParametersUuid = null;
            switch (processConfig.processType()) {
                case SECURITY_ANALYSIS -> {
                    securityAnalysisParametersUuid = ((SecurityAnalysisConfig) processConfig).securityAnalysisParametersUuid();
                    loadflowParametersUuid = ((SecurityAnalysisConfig) processConfig).loadflowParametersUuid();
                }
            }

            processExecutionContexts.put(executionId, new ProcessExecutionContext(
                applyModificationsStepId,
                securityAnalysisStepId,
                caseUuid,
                securityAnalysisParametersUuid,
                loadflowParametersUuid
            ));

            processExecutionService.updateExecutionStatus(executionId, ProcessStatus.RUNNING, null, Instant.now(), null);
            processExecutionService.updateStepStatus(executionId,
                new ProcessExecutionStep(applyModificationsStepId, "APPLY_MODIFICATIONS", 0, StepStatus.RUNNING, null, null, null, Instant.now(), null, null));
            processExecutionService.updateStepStatus(executionId,
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

            processExecutionService.updateStepStatus(executionId,
                new ProcessExecutionStep(processExecutionContext.applyModificationsStepId(), stepType, 0, stepStatus, null, null, reportUuid, null, Instant.now(), caseResultUuid));
            processExecutionService.updateStepStatus(executionId,
                new ProcessExecutionStep(processExecutionContext.securityAnalysisStepId(), "SECURITY_ANALYSIS", 1,
                    stepStatus == StepStatus.COMPLETED ? StepStatus.RUNNING : StepStatus.SKIPPED,
                    null, null, null, Instant.now(), null, null));

            if (stepStatus == StepStatus.COMPLETED) {
                // call security-analysis-server to run security analysis
                securityAnalysisRestService.runSecurityAnalysis(caseResultUuid, executionId, processExecutionContext.securityAnalysisParametersUuid(), processExecutionContext.loadflowParametersUuid);
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

            processExecutionService.updateStepStatus(executionId,
                new ProcessExecutionStep(processExecutionContext.securityAnalysisStepId(), stepType, 1, stepStatus, resultUuid, ResultType.SECURITY_ANALYSIS, reportUuid, null, Instant.now(), caseResultUuid));
            processExecutionService.updateExecutionStatus(executionId,
                stepStatus == StepStatus.COMPLETED ? ProcessStatus.COMPLETED : ProcessStatus.FAILED,
                null, null, Instant.now());

            processExecutionContexts.remove(executionId);
        };
    }
}
