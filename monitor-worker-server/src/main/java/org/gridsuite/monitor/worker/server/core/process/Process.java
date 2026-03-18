/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;

import java.util.List;

/**
 * Definition of a runnable process.
 * <p>
 * A {@code Process} is identified by a {@link ProcessType} and provides an ordered list of {@link ProcessStep}s
 * to be orchestrated by {@code ProcessExecutor}.
 * <p>
 *
 * @param <C> the concrete {@link ProcessConfig} type required by this process
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public interface Process<C extends ProcessConfig> {

    /**
     * The unique process type used to route execution requests to this implementation.
     *
     * @return the process type
     */
    ProcessType getProcessType();

    /**
     * Defines the list of steps that compose this process.
     *
     * @return ordered process steps
     */
    List<ProcessStep<C>> getSteps();

    /**
     * Executes the steps for this process.
     *
     * @param context the current execution context
     * @param stepExecutor the executor responsible for running and skipping individual steps
     */
    void executeSteps(ProcessExecutionContext<C> context, StepExecutor stepExecutor);

    /**
     * Hook invoked when a step throws during execution.
     *
     * @param context the current execution context
     * @param step the step that failed
     * @param e the exception thrown by the step
     */
    void onStepFailure(ProcessExecutionContext<C> context, ProcessStep<C> step, Exception e);
}
