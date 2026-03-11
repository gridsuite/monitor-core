/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.steps.AbstractStepExecutor;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.springframework.stereotype.Service;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class StepExecutionService<C extends ProcessConfig> extends AbstractStepExecutor {

    public StepExecutionService(NotificationService notificationService, ReportService reportService) {
        super(notificationService::updateStepStatus, reportService::sendReport);
    }

    public void skipStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        skipStep(context.getProcessExecutionId(),
                context.getStepExecutionId(),
                step.getType().getName(),
                context.getStepOrder(),
                context.getStartedAt()
        );
    }

    public void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        executeStep(context.getProcessExecutionId(),
                context.getStepExecutionId(),
                step.getType().getName(),
                context.getStepOrder(),
                context.getStartedAt(),
                context.getReportInfos(),
                context.getResultInfos(),
                () -> step.execute(context)
        );
    }

}
