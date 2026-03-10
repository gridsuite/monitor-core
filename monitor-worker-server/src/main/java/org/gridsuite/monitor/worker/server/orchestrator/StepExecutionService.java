/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.steps.AbstractStepExecutor;
import org.gridsuite.monitor.worker.server.clients.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.messaging.MonitorPublisher;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.springframework.stereotype.Service;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class StepExecutionService extends AbstractStepExecutor implements StepExecutor {

    public StepExecutionService(MonitorPublisher notificationService, ReportRestClient reportService) {
        super(notificationService::updateStepStatus, reportService::sendReport);
    }

    @Override
    public <C extends ProcessConfig> void skipStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        skipStep(context.getProcessExecutionId(),
                context.getStepExecutionId(),
                step.getType().getName(),
                context.getStepOrder(),
                context.getStartedAt()
        );
    }

    @Override
    public <C extends ProcessConfig> void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        executeStep(context.getProcessExecutionId(),
                context.getStepExecutionId(),
                step.getType().getName(),
                context.getStepOrder(),
                context.getStartedAt(),
                context.getReportInfos().reportUuid(),
                context.getReportInfos(),
                context.getResultInfos(),
                () -> step.execute(context)
        );
    }
}
