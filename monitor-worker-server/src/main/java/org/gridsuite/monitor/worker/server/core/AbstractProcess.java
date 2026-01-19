/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core;

import lombok.Getter;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.worker.server.services.FilterRestService;
import org.gridsuite.monitor.worker.server.services.FilterService;
import org.gridsuite.monitor.worker.server.services.NetworkConversionService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationRestService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationService;
import org.gridsuite.monitor.worker.server.services.NotificationService;
import org.gridsuite.monitor.worker.server.services.StepExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public abstract class AbstractProcess<C extends ProcessConfig> implements Process<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcess.class);
    protected final ProcessType processType;
    protected final StepExecutionService<C> stepExecutionService;
    protected final NotificationService notificationService;
    protected final NetworkConversionService networkConversionService;
    protected final NetworkModificationService networkModificationService;
    protected final NetworkModificationRestService networkModificationRestService;

    protected final FilterService filterService;
    protected final FilterRestService filterRestService;

    protected AbstractProcess(
            ProcessType processType,
            StepExecutionService<C> stepExecutionService,
            NotificationService notificationService,
            NetworkConversionService networkConversionService,
            NetworkModificationService networkModificationService,
            NetworkModificationRestService networkModificationRestService,
            FilterService filterService,
            FilterRestService filterRestService) {
        this.processType = processType;
        this.stepExecutionService = stepExecutionService;
        this.notificationService = notificationService;
        this.networkConversionService = networkConversionService;
        this.networkModificationService = networkModificationService;
        this.networkModificationRestService = networkModificationRestService;
        this.filterService = filterService;
        this.filterRestService = filterRestService;
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
        LOGGER.error("Execution id: {} - Step failed: {} - {}", context.getExecutionId(), step.getType(), e.getMessage());
    }
}
