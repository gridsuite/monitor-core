/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.orchestrator;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStep;
import org.gridsuite.monitor.commons.api.types.processexecution.StepStatus;
import org.gridsuite.monitor.worker.server.client.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.gridsuite.monitor.worker.server.messaging.NotificationService;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class StepExecutionService implements StepExecutor {

    private final NotificationService notificationService;
    private final ReportRestClient reportRestClient;

    @Override
    public <C extends ProcessConfig> void skipStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        ProcessExecutionStep executionStep = ProcessExecutionStep.builder()
                .id(context.getStepExecutionId())
                .stepType(step.getType().getName())
                .stepOrder(context.getStepOrder())
                .status(StepStatus.SKIPPED)
                .startedAt(context.getStartedAt())
                .completedAt(Instant.now())
                .build();
        notificationService.updateStepStatus(context.getProcessExecutionId(), executionStep);
    }

    @Override
    public <C extends ProcessConfig> void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        ProcessExecutionStep executionStep = ProcessExecutionStep.builder()
                .id(context.getStepExecutionId())
                .stepType(step.getType().getName())
                .stepOrder(context.getStepOrder())
                .status(StepStatus.RUNNING)
                .reportId(context.getReportInfos().reportUuid())
                .startedAt(context.getStartedAt())
                .build();
        notificationService.updateStepStatus(context.getProcessExecutionId(), executionStep);

        try {
            step.execute(context);
            reportRestClient.sendReport(context.getReportInfos());
            updateStepStatus(context, StepStatus.COMPLETED, step);
        } catch (Exception e) {
            updateStepStatus(context, StepStatus.FAILED, step);
            throw e;
        }
    }

    private void updateStepStatus(ProcessStepExecutionContext<?> context, StepStatus status, ProcessStep<?> step) {
        ProcessExecutionStep updated = ProcessExecutionStep.builder()
                .id(context.getStepExecutionId())
                .stepType(step.getType().getName())
                .stepOrder(context.getStepOrder())
                .status(status)
                .resultId(context.getResultInfos() != null ? context.getResultInfos().resultUUID() : null)
                .resultType(context.getResultInfos() != null ? context.getResultInfos().resultType() : null)
                .reportId(context.getReportInfos().reportUuid())
                .startedAt(context.getStartedAt())
                .completedAt(Instant.now())
                .build();
        notificationService.updateStepStatus(context.getProcessExecutionId(), updated);
    }
}
