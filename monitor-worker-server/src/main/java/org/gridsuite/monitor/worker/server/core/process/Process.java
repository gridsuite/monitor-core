/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;

import java.util.List;

/**
 * Definition of a runnable process.
 * <p>
 * A {@code Process} is identified by a {@link ProcessType} and provides an ordered list of {@link ProcessStep}s
 * to be orchestrated by {@code ProcessExecutor}.
 * <p>
 * This interface intentionally does not prescribe how steps are executed (status updates, retries, skipping policy,
 * messaging, etc.). Those concerns belong to the orchestration layer.
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
     * Defines the ordered list of steps that compose this process.
     * <p>
     * The returned list order is the execution order used by the orchestrator.
     *
     * @return ordered process steps
     */
    List<ProcessStep<C>> defineSteps();

    /**
     * Hook invoked by the orchestrator when a step throws during execution.
     * <p>
     * Implementations may log, enrich domain-level diagnostics, or perform compensating actions.
     * The default implementation is a no-op.
     *
     * @param context the current execution context
     * @param step the step that failed
     * @param e the exception thrown by the step
     */
    default void onStepFailure(ProcessExecutionContext<C> context, ProcessStep<C> step, Exception e) {
        // default no-op (or log in orchestrator)
    }
}
