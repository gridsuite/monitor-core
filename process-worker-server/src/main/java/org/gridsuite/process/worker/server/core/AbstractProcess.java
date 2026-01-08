/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.core;

import lombok.Getter;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.worker.server.services.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public abstract class AbstractProcess<C extends ProcessConfig> implements Process<C> {

    protected final ProcessType processType;
    protected final StepExecutionService<C> stepExecutionService;
    protected final NotificationService notificationService;
    protected final NetworkConversionService networkConversionService;

    protected AbstractProcess(
            ProcessType processType,
            StepExecutionService<C> stepExecutionService,
            NotificationService notificationService,
            NetworkConversionService networkConversionService) {
        this.processType = processType;
        this.stepExecutionService = stepExecutionService;
        this.notificationService = notificationService;
        this.networkConversionService = networkConversionService;
    }

    @Override
    public void execute(ProcessExecutionContext<C> context) {
        List<ProcessStep<C>> steps = defineSteps();
        boolean skipRemaining = false;
        UUID previousStepId = null;

        for (ProcessStep<C> step : steps) {
            ProcessStepExecutionContext<C> stepContext = context.createStepContext(step, previousStepId);
            previousStepId = stepContext.getStepExecutionId();

            if (skipRemaining) {
                stepExecutionService.skipStep(stepContext, step);
                continue;
            }

            try {
                stepExecutionService.executeStep(stepContext, step);
            } catch (Exception e) {
                handleStepFailure(context, step, e);
                skipRemaining = true;
            }
        }
    }

    protected abstract List<ProcessStep<C>> defineSteps();

    protected void handleStepFailure(ProcessExecutionContext<C> context, ProcessStep<C> step, Exception e) {
        //TODO better error handling
        e.printStackTrace();
    }
}
