/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.StepStatus;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class StepExecutionService<C extends ProcessConfig> {

    private final NotificationService notificationService;
    private final ReportService reportService;

    public void skipStep(ProcessStepExecutionContext<?> context, ProcessStep<?> step) {
        ProcessExecutionStep executionStep = ProcessExecutionStep.builder()
                .id(context.getStepExecutionId())
                .stepType(step.getType().getName())
                .stepOrder(context.getStepOrder())
                .status(StepStatus.SKIPPED)
                .startedAt(context.getStartedAt())
                .completedAt(Instant.now())
                .build();
        notificationService.notifyStep(context.getProcessExecutionId(), executionStep);
    }

    public void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        ProcessExecutionStep executionStep = ProcessExecutionStep.builder()
                .id(context.getStepExecutionId())
                .stepType(step.getType().getName())
                .stepOrder(context.getStepOrder())
                .status(StepStatus.RUNNING)
                .reportId(context.getReportInfos().reportUuid())
                .startedAt(context.getStartedAt())
                .build();
        notificationService.notifyStep(context.getProcessExecutionId(), executionStep);

        try {
            step.execute(context);
            reportService.sendReport(context.getReportInfos());
            publishStep(context, StepStatus.COMPLETED, step);
        } catch (Exception e) {
            publishStep(context, StepStatus.FAILED, step);
            throw e;
        }
    }

    private void publishStep(ProcessStepExecutionContext<?> context, StepStatus status, ProcessStep<?> step) {
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
        notificationService.notifyStep(context.getProcessExecutionId(), updated);
    }
}
