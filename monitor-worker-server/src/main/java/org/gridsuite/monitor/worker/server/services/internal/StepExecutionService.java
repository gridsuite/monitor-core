/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.internal;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.StepStatus;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.services.external.client.ReportRestClient;
import org.gridsuite.monitor.worker.server.services.messaging.NotificationService;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class StepExecutionService<C extends ProcessConfig> {

    private final NotificationService notificationService;
    private final ReportRestClient reportRestClient;

    public void skipStep(ProcessStepExecutionContext<?> context, ProcessStep<?> step) {
        ProcessExecutionStep executionStep = new ProcessExecutionStep(
                context.getStepExecutionId(),
                step.getType().getName(),
                context.getStepOrder(),
                StepStatus.SKIPPED,
                null,
                null,
                null,
                context.getStartedAt(),
                Instant.now()
        );
        notificationService.updateStepStatus(context.getProcessExecutionId(), executionStep);
    }

    public void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        ProcessExecutionStep executionStep = new ProcessExecutionStep(
            context.getStepExecutionId(),
            step.getType().getName(),
            context.getStepOrder(),
            StepStatus.RUNNING,
            null,
            null,
            context.getReportInfos().reportUuid(),
            context.getStartedAt(),
            null
        );
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
        ProcessExecutionStep updated = new ProcessExecutionStep(
            context.getStepExecutionId(),
            step.getType().getName(),
            context.getStepOrder(),
            status,
            context.getResultInfos() != null ? context.getResultInfos().resultUUID() : null,
            context.getResultInfos() != null ? context.getResultInfos().resultType() : null,
            context.getReportInfos().reportUuid(),
            context.getStartedAt(),
            Instant.now()
        );
        notificationService.updateStepStatus(context.getProcessExecutionId(), updated);
    }
}
