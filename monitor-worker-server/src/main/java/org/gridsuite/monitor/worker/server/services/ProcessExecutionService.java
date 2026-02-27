/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.ProcessRunMessage;
import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.StepStatus;
import org.gridsuite.monitor.worker.server.core.Process;
import org.gridsuite.monitor.worker.server.core.ProcessExecutionContext;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
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
public class ProcessExecutionService {

    private final Map<ProcessType, Process<? extends ProcessConfig>> processes;
    private final NotificationService notificationService;
    private final String executionEnvName;

    public ProcessExecutionService(List<Process<? extends ProcessConfig>> processList,
                                   NotificationService notificationService,
                                   @Value("${worker.execution-env-name:default-env}") String executionEnvName) {
        this.processes = processList.stream()
            .collect(Collectors.toMap(Process::getProcessType, w -> w));
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
        notificationService.notifySteps(context.getExecutionId(),
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
        process.execute(context);
        updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.COMPLETED);
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
