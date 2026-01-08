/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.worker.server.core.Process;
import org.gridsuite.monitor.worker.server.core.ProcessExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ProcessExecutionService {

    private final Map<ProcessType, Process<ProcessConfig>> processes;
    private final NotificationService notificationService;
    private final String executionEnvName;

    public ProcessExecutionService(List<Process<ProcessConfig>> processList,
                                   NotificationService notificationService,
                                   @Value("${worker.execution-env-name:default-env}") String executionEnvName) {
        this.processes = processList.stream()
            .collect(Collectors.toMap(Process::getProcessType, w -> w));
        this.notificationService = notificationService;
        this.executionEnvName = executionEnvName;
    }

    public void executeProcess(ProcessConfig config) {
        Process<ProcessConfig> process = processes.get(config.processType());
        if (process == null) {
            throw new IllegalArgumentException("No process found for type: " + config.processType());
        }

        ProcessExecutionContext<ProcessConfig> context = createExecutionContext(config, executionEnvName);

        updateExecutionStatus(context, ProcessStatus.RUNNING);

        try {
            process.execute(context);
            updateExecutionStatus(context, ProcessStatus.COMPLETED);
        } catch (Exception e) {
            updateExecutionStatus(context, ProcessStatus.FAILED);
            throw e;
        }
    }

    private void updateExecutionStatus(ProcessExecutionContext<ProcessConfig> context, ProcessStatus status) {
        ProcessExecutionStatusUpdate processExecutionStatusUpdate = new ProcessExecutionStatusUpdate(
            status,
            context.getExecutionEnvName(),
            status == ProcessStatus.COMPLETED || status == ProcessStatus.FAILED ? Instant.now() : null
        );

        notificationService.updateExecutionStatus(context.getExecutionId(), processExecutionStatusUpdate);
    }

    private ProcessExecutionContext<ProcessConfig> createExecutionContext(ProcessConfig config, String executionEnvName) {
        return new ProcessExecutionContext<>(config, executionEnvName);
    }
}
