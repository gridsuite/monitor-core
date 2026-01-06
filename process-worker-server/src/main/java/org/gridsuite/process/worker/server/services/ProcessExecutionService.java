package org.gridsuite.process.worker.server.services;

import org.gridsuite.process.commons.ProcessExecutionStatusUpdate;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.worker.server.core.Process;
import org.gridsuite.process.worker.server.core.ProcessExecutionContext;
import org.gridsuite.process.commons.ProcessStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void executeProcess(ProcessConfig config) {
        Process<ProcessConfig> process = (Process<ProcessConfig>) processes.get(config.processType());
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
