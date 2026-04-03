/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator;

import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.orchestrator.context.ProcessExecutionContext;
import org.gridsuite.monitor.server.orchestrator.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.server.orchestrator.process.Process;
import org.gridsuite.monitor.server.orchestrator.process.ProcessStep;
import org.gridsuite.monitor.server.services.processconfig.ProcessConfigService;
import org.gridsuite.monitor.server.services.processexecution.ProcessExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Orchestrates the execution of a process by iterating over its steps.
 * Supports both synchronous (blocking) and asynchronous (fire-and-forget) steps.
 * <p>
 * For async steps, the orchestrator stops the loop after firing and waits for a
 * RabbitMQ callback via {@link #resumeAfterAsyncStep(AsyncStepResult)}.
 * <p>
 * Moved from monitor-worker-server and adapted for server-side execution.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ProcessOrchestrationService implements ProcessExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessOrchestrationService.class);

    private final Map<ProcessType, Process<? extends ProcessConfig>> processes;
    private final StepExecutor stepExecutor;
    private final Notificator notificationService;
    private final ProcessExecutionService processExecutionService;
    private final ProcessConfigService processConfigService;
    private final String executionEnvName;

    public ProcessOrchestrationService(List<Process<? extends ProcessConfig>> processList,
                                       StepExecutor stepExecutor,
                                       Notificator notificationService,
                                       ProcessExecutionService processExecutionService,
                                       ProcessConfigService processConfigService,
                                       @Value("${monitor.execution-env-name:default-env}") String executionEnvName) {
        this.processes = processList.stream()
            .collect(Collectors.toMap(Process::getProcessType, w -> w));
        this.stepExecutor = stepExecutor;
        this.notificationService = notificationService;
        this.processExecutionService = processExecutionService;
        this.processConfigService = processConfigService;
        this.executionEnvName = executionEnvName;
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
            executionEnvName,
            runMessage.debugFileLocation()
        );

        try {
            initializeSteps(process, context);
        } catch (Exception e) {
            updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.FAILED);
            throw e;
        }

        updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.RUNNING);
        executeStepsFrom(process, context, 0);
    }

    @Override
    public void resumeAfterAsyncStep(AsyncStepResult result) {
        ProcessExecutionEntity entity = processExecutionService.findById(result.executionId())
            .orElseThrow(() -> new IllegalStateException("Execution not found: " + result.executionId()));

        ProcessConfig config = processConfigService.getProcessConfig(entity.getProcessConfigId())
            .orElseThrow(() -> new IllegalStateException("Config not found: " + entity.getProcessConfigId()))
            .processConfig();

        ProcessType processType = ProcessType.valueOf(entity.getType());

        completeAsyncStep(entity, result);

        if (!result.success()) {
            updateExecutionStatus(result.executionId(), entity.getExecutionEnvName(), ProcessStatus.FAILED);
            doSkipRemainingSteps(processType, config, entity, result);
            return;
        }

        doResumeExecution(processType, config, entity, result);
    }

    @SuppressWarnings("unchecked")
    private <T extends ProcessConfig> void doResumeExecution(ProcessType processType, ProcessConfig config,
                                                             ProcessExecutionEntity entity, AsyncStepResult result) {
        Process<T> process = (Process<T>) processes.get(processType);
        ProcessExecutionContext<T> context = reconstructContext(result, (T) config, entity);
        executeStepsFrom(process, context, result.completedStepIndex() + 1);
    }

    @SuppressWarnings("unchecked")
    private <T extends ProcessConfig> void doSkipRemainingSteps(ProcessType processType, ProcessConfig config,
                                                                ProcessExecutionEntity entity, AsyncStepResult result) {
        Process<T> process = (Process<T>) processes.get(processType);
        ProcessExecutionContext<T> context = reconstructContext(result, (T) config, entity);
        skipRemainingSteps(process, context, result.completedStepIndex() + 1);
    }

    private <T extends ProcessConfig> ProcessExecutionContext<T> reconstructContext(AsyncStepResult result,
                                                                                    T config,
                                                                                    ProcessExecutionEntity entity) {
        ProcessExecutionContext<T> context = new ProcessExecutionContext<>(
            result.executionId(),
            entity.getCaseUuid(),
            config,
            entity.getExecutionEnvName(),
            entity.getDebugFileLocation()
        );
        context.setCaseS3Key(result.caseS3Key());
        return context;
    }

    private void completeAsyncStep(ProcessExecutionEntity entity, AsyncStepResult result) {
        ProcessExecutionStepEntity stepEntity = entity.getSteps().stream()
            .filter(s -> s.getStepOrder() == result.completedStepIndex())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Step not found at index " + result.completedStepIndex() + " for execution " + result.executionId()));

        ProcessExecutionStep completed = ProcessExecutionStep.builder()
            .id(stepEntity.getId())
            .stepType(stepEntity.getStepType())
            .stepOrder(stepEntity.getStepOrder())
            .status(result.success() ? StepStatus.COMPLETED : StepStatus.FAILED)
            .resultId(result.resultUuid())
            .resultType(result.resultType())
            .reportId(stepEntity.getReportId())
            .startedAt(stepEntity.getStartedAt())
            .completedAt(Instant.now())
            .build();

        notificationService.updateStepStatus(result.executionId(), completed);
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

    /**
     * Executes steps starting from {@code fromIndex}.
     * <p>
     * For sync steps: executes and continues to the next step.
     * For async steps: fires the step and returns immediately (thread released).
     * Execution will resume via {@link #resumeAfterAsyncStep(AsyncStepResult)}.
     */
    private <T extends ProcessConfig> void executeStepsFrom(Process<T> process, ProcessExecutionContext<T> context, int fromIndex) {
        List<ProcessStep<T>> steps = process.getSteps();

        for (int i = fromIndex; i < steps.size(); i++) {
            ProcessStep<T> step = steps.get(i);
            ProcessStepExecutionContext<T> stepContext = context.createStepContext(step, i);

            try {
                stepExecutor.executeStep(stepContext, step);
            } catch (Exception e) {
                LOGGER.error("Execution id: {} - Step failed: {} - {}", context.getExecutionId(), step.getType(), e.getMessage());
                updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.FAILED);
                skipRemainingSteps(process, context, i + 1);
                return;
            }

            if (step.isAsync()) {
                // Async step was fired; execution will resume via callback
                return;
            }
        }

        // All steps completed successfully
        updateExecutionStatus(context.getExecutionId(), context.getExecutionEnvName(), ProcessStatus.COMPLETED);
    }

    private <T extends ProcessConfig> void skipRemainingSteps(Process<T> process, ProcessExecutionContext<T> context, int fromIndex) {
        List<ProcessStep<T>> steps = process.getSteps();
        for (int i = fromIndex; i < steps.size(); i++) {
            ProcessStep<T> step = steps.get(i);
            ProcessStepExecutionContext<T> stepContext = context.createStepContext(step, i);
            stepExecutor.skipStep(stepContext, step);
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
