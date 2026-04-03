/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator.process;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

import java.util.List;

/**
 * Definition of a runnable process.
 * <p>
 * A {@code Process} is identified by a {@link ProcessType} and provides an ordered list of {@link ProcessStep}s
 * to be orchestrated by {@code ProcessExecutor}.
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
     *
     * @return ordered process steps
     */
    List<ProcessStep<C>> getSteps();
}
