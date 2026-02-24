/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;

import java.util.UUID;

/**
 * Definition of a single step within a {@link Process}.
 * <p>
 * A step is:
 * <ul>
 *   <li>Identified by a unique {@link #getId()} (used for monitoring/correlation)</li>
 *   <li>Classified by a {@link #getType()} whose name is used in external status updates</li>
 *   <li>Executed with a {@link ProcessStepExecutionContext} that provides access to the run configuration and shared state</li>
 * </ul>
 * <p>
 * Cross-cutting concerns (status updates, reporting, skipping policy) are handled by the orchestration layer; the step
 * implementation should focus on its business logic and may update the context (e.g. network/result info) as needed.
 *
 * @param <C> the concrete {@link ProcessConfig} type required by this step
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public interface ProcessStep<C extends ProcessConfig> {

    /**
     * The functional type of this step.
     *
     * @return the step type
     */
    ProcessStepType getType();

    /**
     * Unique identifier of the step instance within a process definition.
     * <p>
     * This identifier is typically generated once (e.g. in {@code AbstractProcessStep}) and reused in status updates.
     *
     * @return the step id
     */
    UUID getId();

    /**
     * Executes the step business logic.
     * <p>
     * Implementations may read configuration and shared state from {@code context} and may write outputs back to the context
     * (e.g. set network/result information).
     *
     * @param context step execution context for the current run
     * @throws RuntimeException any failure should be propagated; the orchestrator/step executor is responsible for
     *                          translating it into monitoring status updates and deciding what to do next
     */
    void execute(ProcessStepExecutionContext<C> context);
}
