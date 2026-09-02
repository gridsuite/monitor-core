/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.worker.server.clients.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.messaging.Notificator;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class StepExecutionService implements StepExecutor {
    private final Notificator notificationService;
    private final ReportRestClient reportRestClient;

    @Override
    public <C extends ProcessConfig> void initializeSteps(UUID processExecutionId, List<ProcessStepExecutionContext<C>> stepsContexts) {
        updateExecutionStepsStatuses(processExecutionId, stepsContexts, StepStatus.SCHEDULED);
    }

    @Override
    public <C extends ProcessConfig> void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        updateStepStatus(context, StepStatus.RUNNING);

        try {
            step.execute(context);
            updateStepStatus(context, StepStatus.COMPLETED);
        } catch (Exception e) {
            updateStepStatus(context, StepStatus.FAILED);
            throw e;
        } finally {
            reportRestClient.sendReport(context.getProcessReportId(), context.getReportNode());
        }
    }

    @Override
    public <C extends ProcessConfig> void skipSteps(UUID processExecutionId, List<ProcessStepExecutionContext<C>> stepsContexts) {
        updateExecutionStepsStatuses(processExecutionId, stepsContexts, StepStatus.SKIPPED);
    }

    private <C extends ProcessConfig> void updateStepStatus(ProcessStepExecutionContext<C> context, StepStatus status) {
        ProcessExecutionStep updatedStep = ProcessExecutionStep.builder()
                .id(context.getStepExecutionId())
                .stepType(context.getProcessStepType().getName())
                .stepOrder(context.getStepOrder())
                .status(status)
                .resultId(context.getResultInfos() != null ? context.getResultInfos().resultUUID() : null)
                .resultType(context.getResultInfos() != null ? context.getResultInfos().resultType() : null)
                .startedAt(context.getStartedAt())
                .completedAt(status == StepStatus.COMPLETED || status == StepStatus.FAILED ? Instant.now() : null)
                .build();

        notificationService.updateStepStatus(context.getProcessExecutionId(), updatedStep);
    }

    private <C extends ProcessConfig> void updateExecutionStepsStatuses(UUID processExecutionId, List<ProcessStepExecutionContext<C>> stepsContexts, StepStatus status) {
        List<ProcessExecutionStep> updatedSteps = stepsContexts.stream().map(
                context -> ProcessExecutionStep.builder()
                        .id(context.getStepExecutionId())
                        .stepType(context.getProcessStepType().getName())
                        .stepOrder(context.getStepOrder())
                        .status(status)
                        .startedAt(status == StepStatus.SKIPPED ? Instant.now() : null)
                        .completedAt(status == StepStatus.SKIPPED ? Instant.now() : null)
                        .build()
        ).toList();

        notificationService.updateStepsStatuses(processExecutionId, updatedSteps);
    }
}
