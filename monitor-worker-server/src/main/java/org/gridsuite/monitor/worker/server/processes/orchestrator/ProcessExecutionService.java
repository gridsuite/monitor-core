/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.orchestrator;

import org.gridsuite.monitor.commons.api.types.message.ProcessRunMessage;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.*;
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

    public ProcessExecutionService(List<Process<? extends ProcessConfig>> processList,
                                   StepExecutor stepExecutor,
                                   Notificator notificationService,
                                   @Value("${worker.execution-env-name:default-env}") String executionEnvName) {
        this.processes = processList.stream()
            .collect(Collectors.toMap(Process::getProcessType, w -> w));
        this.stepExecutor = stepExecutor;
        this.notificationService = notificationService;
        this.executionEnvName = executionEnvName;
    }

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
            executionEnvName,
            runMessage.debugFileLocation()
        );

        try {
            initializeSteps(process, context);
            executeSteps(process, context);
        } catch (Exception e) {
            updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.FAILED);
            throw e;
        }
    }

    private <T extends ProcessConfig> void initializeSteps(Process<T> process, ProcessExecutionContext<T> context) {
        List<ProcessStep<T>> steps = process.getSteps();
        notificationService.updateStepsStatuses(context.getExecutionId(),
                IntStream.range(0, steps.size())
                        .mapToObj(i -> ProcessExecutionStep.builder()
                                .id(steps.get(i).getId())
                                .stepType(steps.get(i).getType().getName())
                                .stepOrder(i)
                                .status(StepStatus.SCHEDULED)
                                .build())
                        .toList());
    }

    private <T extends ProcessConfig> void executeSteps(Process<T> process, ProcessExecutionContext<T> context) {
        updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.RUNNING);
        doExecuteSteps(process, context);
        updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.COMPLETED);
    }

    private <T extends ProcessConfig> void doExecuteSteps(Process<T> process, ProcessExecutionContext<T> context) {
        List<ProcessStep<T>> steps = process.getSteps();
        boolean skipRemaining = false;

        for (int i = 0; i < steps.size(); i++) {
            ProcessStep<T> step = steps.get(i);
            ProcessStepExecutionContext<T> stepContext = context.createStepContext(step, i);

            if (skipRemaining) {
                stepExecutor.skipStep(stepContext, step);
                continue;
            }

            try {
                stepExecutor.executeStep(stepContext, step);
            } catch (Exception e) {
                process.onStepFailure(context, step, e);
                skipRemaining = true;
            }
        }
    }

    private void updateExecutionStatus(UUID executionId, String envName, ProcessStatus status) {
        ProcessExecutionStatusUpdate processExecutionStatusUpdate = new ProcessExecutionStatusUpdate(
            status,
            envName,
            status == ProcessStatus.RUNNING ? Instant.now() : null,
            status == ProcessStatus.COMPLETED || status == ProcessStatus.FAILED ? Instant.now() : null
        );

        notificationService.updateExecutionStatus(executionId, processExecutionStatusUpdate);
    }
}
