/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.orchestrator;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;

import java.util.List;
import java.util.UUID;

/**
 * Child orchestrator interface responsible for executing a {@link ProcessStep} within a process run.
 *
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public interface StepExecutor {

    /**
     * Initialize the given steps and publish step status updates.
     *
     * @param executionId step execution context
     * @param steps steps definition to initialize
     * @param <C> concrete {@link ProcessConfig} type associated with the parent process
     * @throws RuntimeException any exception thrown by the step implementation is propagated after updating status
     */
    <C extends ProcessConfig> void initializeSteps(UUID executionId, List<ProcessStep<C>> steps);

    /**
     * Execute a step and publish step status updates around the execution.
     *
     * @param context step execution context
     * @param step step definition to execute
     * @param <C> concrete {@link ProcessConfig} type associated with the parent process
     * @throws RuntimeException any exception thrown by the step implementation is propagated after updating status
     */
    <C extends ProcessConfig> void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step);

    /**
     * Mark steps as skipped and publish the corresponding update.
     *
     * @param executionId step execution context
     * @param steps all steps definition of a process
     * @param <C> concrete {@link ProcessConfig} type associated with the parent process
     * @param fromIndex index of the first step to skip
     * @throws RuntimeException any exception thrown by the step implementation is propagated after updating status
     */
    <C extends ProcessConfig> void skipSteps(UUID executionId, List<ProcessStep<C>> steps, int fromIndex);
}
