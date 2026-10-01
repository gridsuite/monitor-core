/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.*;
import org.gridsuite.monitor.worker.server.clients.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.messaging.Notificator;
import org.gridsuite.monitor.worker.server.core.orchestrator.ProcessExecutor;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.gridsuite.monitor.worker.server.core.process.Process;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ProcessExecutionService implements ProcessExecutor {

    private final Map<ProcessType, Process<? extends ProcessConfig>> processes;
    private final StepExecutor stepExecutor;
    private final Notificator notificationService;
    private final String executionEnvName;
    private final ReportRestClient reportRestClient;

    public ProcessExecutionService(List<Process<? extends ProcessConfig>> processList,
                                   StepExecutor stepExecutor,
                                   Notificator notificationService,
                                   ReportRestClient reportRestClient,
                                   @Value("${worker.execution-env-name:default-env}") String executionEnvName) {
        this.processes = processList.stream()
            .collect(Collectors.toMap(Process::getProcessType, w -> w));
        this.stepExecutor = stepExecutor;
        this.notificationService = notificationService;
        this.executionEnvName = executionEnvName;
        this.reportRestClient = reportRestClient;
    }

    @Override
    public <T extends ProcessConfig> void executeProcess(ProcessRunMessage<T> runMessage) {
        @SuppressWarnings("unchecked") // safe: ProcessType uniquely maps to a Process with the matching ProcessConfig subtype
        Process<T> process = (Process<T>) processes.get(runMessage.processType());
        if (process == null) {
            throw new IllegalArgumentException("No process found for type: " + runMessage.processType());
        }

        ProcessExecutionContext<T> context = new ProcessExecutionContext<>(
            runMessage.executionId(),
            runMessage.caseUuid(),
            runMessage.config(),
            runMessage.reportId(),
            executionEnvName,
            runMessage.debugFileLocation()
        );

        updateExecutionStatus(context, ProcessStatus.RUNNING);

        try {
            reportRestClient.sendReport(context.getReportId(), context.getReportNode());
            List<ProcessStep<T>> processSteps = process.getSteps();
            List<ProcessStepExecutionContext<T>> stepContexts = IntStream.range(0, processSteps.size())
                    .mapToObj(i -> context.createStepContext(processSteps.get(i).getType(), i))
                    .toList();
            initializeSteps(context.getExecutionId(), stepContexts);
            executeSteps(context.getExecutionId(), processSteps, stepContexts);
            updateExecutionStatus(context, ProcessStatus.COMPLETED);
        } catch (Exception e) {
            updateExecutionStatus(context, ProcessStatus.FAILED);
            throw e;
        }
    }

    private <T extends ProcessConfig> void initializeSteps(UUID executionId, List<ProcessStepExecutionContext<T>> stepContexts) {
        updateExecutionStepsStatuses(executionId, stepContexts, StepStatus.SCHEDULED, 0);
    }

    private <T extends ProcessConfig> void executeSteps(UUID executionId, List<ProcessStep<T>> processSteps, List<ProcessStepExecutionContext<T>> stepContexts) {
        for (int i = 0; i < processSteps.size(); i++) {
            try {
                stepExecutor.executeStep(stepContexts.get(i), processSteps.get(i));
            } catch (Exception e) {
                skipRemainingSteps(executionId, stepContexts, i + 1);
                throw e;
            }
        }
    }

    private <T extends ProcessConfig> void skipRemainingSteps(UUID executionId, List<ProcessStepExecutionContext<T>> stepContexts, int fromIndex) {
        updateExecutionStepsStatuses(executionId, stepContexts, StepStatus.SKIPPED, fromIndex);
    }

    private <T extends ProcessConfig> void updateExecutionStatus(ProcessExecutionContext<T> context, ProcessStatus status) {
        ProcessExecutionStatusUpdate processExecutionStatusUpdate = new ProcessExecutionStatusUpdate(
            context.getConfig().processType(),
            status,
            context.getExecutionEnvName(),
            status == ProcessStatus.RUNNING ? Instant.now() : null,
            status == ProcessStatus.COMPLETED || status == ProcessStatus.FAILED ? Instant.now() : null
        );

        notificationService.updateExecutionStatus(context.getExecutionId(), processExecutionStatusUpdate);
    }

    private <T extends ProcessConfig> void updateExecutionStepsStatuses(UUID executionId, List<ProcessStepExecutionContext<T>> stepContexts, StepStatus status, int fromIndex) {
        if (fromIndex >= stepContexts.size()) {
            return;
        }
        List<ProcessExecutionStep> updatedSteps = IntStream.range(fromIndex, stepContexts.size())
                .mapToObj(i -> ProcessExecutionStep.builder()
                        .id(stepContexts.get(i).getStepExecutionId())
                        .stepType(stepContexts.get(i).getProcessStepType().getName())
                        .stepOrder(stepContexts.get(i).getStepOrder())
                        .status(status)
                        .startedAt(status == StepStatus.SKIPPED ? Instant.now() : null)
                        .completedAt(status == StepStatus.SKIPPED ? Instant.now() : null)
                        .build())
                .toList();

        notificationService.updateStepsStatuses(executionId, updatedSteps);
    }
}
