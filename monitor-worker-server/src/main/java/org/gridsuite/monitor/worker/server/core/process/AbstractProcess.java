/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import lombok.Getter;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Base class for {@link Process} implementations.
 * <p>
 * Provides:
 * <ul>
 *   <li>Storage of the {@link ProcessType} used to route execution requests</li>
 *   <li>A default {@link #onStepFailure(ProcessExecutionContext, ProcessStep, Exception)} implementation that logs</li>
 * </ul>
 * <p>
 * Note: This class does <strong>not</strong> orchestrate step execution. Orchestration belongs to the application layer
 * (e.g. a {@code ProcessExecutor} / orchestrator service).
 *
 * @param <C> the concrete {@link ProcessConfig} type required by this process
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public abstract class AbstractProcess<C extends ProcessConfig> implements Process<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcess.class);

    /**
     * The process type associated with this implementation.
     */
    protected final ProcessType processType;

    protected AbstractProcess(ProcessType processType) {
        this.processType = processType;
    }

    protected abstract List<ProcessStep<C>> defineSteps();

    @Override
    public List<ProcessStep<C>> getSteps() {
        return Collections.unmodifiableList(defineSteps());
    }

    /**
     * Default failure hook that logs the failing step and exception message.
     * <p>
     * Override to implement domain-specific failure behavior (e.g. cleanup, extra diagnostics).
     */
    @Override
    public void onStepFailure(ProcessExecutionContext<C> context, ProcessStep<C> step, Exception e) {
        // TODO better error handling
        LOGGER.error("Execution id: {} - Step failed: {} - {}", context.getExecutionId(), step.getType(), e.getMessage());
    }
}
