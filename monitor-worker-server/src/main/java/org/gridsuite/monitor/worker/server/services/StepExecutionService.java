/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import lombok.Getter;
import lombok.Setter;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.steps.ReportPublisher;
import org.gridsuite.monitor.commons.steps.StepExecutionInterface;
import org.gridsuite.monitor.commons.steps.StepStatusPublisher;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.ReportInfos;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class StepExecutionService<C extends ProcessConfig> implements StepExecutionInterface<ReportInfos> {

    @Getter
    @Setter
    private UUID executionId;

    private final StepStatusPublisher stepStatusPublisher;
    private final ReportPublisher<ReportInfos> reportPublisher;

    public StepExecutionService(NotificationService notificationService, ReportService reportService) {
        this.stepStatusPublisher = notificationService::updateStepStatus;
        this.reportPublisher = reportService::sendReport;
    }

    @Override
    public StepStatusPublisher getStepStatusPublisher() {
        return stepStatusPublisher;
    }

    @Override
    public ReportPublisher<ReportInfos> getReportPublisher() {
        return reportPublisher;
    }

    public void skipStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        setExecutionId(context.getProcessExecutionId());
        skipStep(
                context.getStepExecutionId(),
                step.getType().getName(),
                context.getStepOrder(),
                context.getStartedAt()
        );
    }

    public void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        setExecutionId(context.getProcessExecutionId());
        executeStep(
                context.getStepExecutionId(),
                step.getType().getName(),
                context.getStepOrder(),
                context.getStartedAt(),
                Objects.requireNonNull(context.getReportInfos()).reportUuid(),
                context.getReportInfos(),
                context.getResultInfos(),
                () -> step.execute(context)
        );
    }

}
