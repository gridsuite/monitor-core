/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import lombok.Getter;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public abstract class AbstractProcess<C extends ProcessConfig> implements Process<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcess.class);

    protected final ProcessType processType;

    protected AbstractProcess(ProcessType processType) {
        this.processType = processType;
    }

    protected abstract List<ProcessStep<C>> defineSteps();

    @Override
    public List<ProcessStep<C>> getSteps() {
        return Collections.unmodifiableList(defineSteps());
    }

    @Override
    public void executeSteps(ProcessExecutionContext<C> context, StepExecutor stepExecutor) {
        List<ProcessStep<C>> steps = getSteps();
        boolean skipRemaining = false;

        for (int i = 0; i < steps.size(); i++) {
            ProcessStep<C> step = steps.get(i);
            ProcessStepExecutionContext<C> stepContext = context.createStepContext(step, i);

            if (skipRemaining) {
                stepExecutor.skipStep(stepContext, step);
                continue;
            }

            try {
                stepExecutor.executeStep(stepContext, step);
            } catch (Exception e) {
                onStepFailure(context, step, e);
                skipRemaining = true;
            }
        }
    }

    @Override
    public void onStepFailure(ProcessExecutionContext<C> context, ProcessStep<C> step, Exception e) {
        // TODO better error handling
        LOGGER.error("Execution id: {} - Step failed: {} - {}", context.getExecutionId(), step.getType(), e.getMessage());
    }
}
