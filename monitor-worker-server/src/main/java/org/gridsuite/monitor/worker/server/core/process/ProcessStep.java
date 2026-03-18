/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;

import java.util.UUID;

/**
 * Definition of a single step within a {@link Process}.
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
     *
     * @return the step id
     */
    UUID getId();

    /**
     * Executes the step business logic.
     *
     * @param context step execution context for the current run
     * @throws RuntimeException any failure should be propagated; the orchestrator/step executor is responsible for
     *                          translating it into monitoring status updates and deciding what to do next
     */
    void execute(ProcessStepExecutionContext<C> context);
}
