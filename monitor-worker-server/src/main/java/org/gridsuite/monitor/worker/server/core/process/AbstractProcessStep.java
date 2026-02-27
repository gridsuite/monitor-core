/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import lombok.Getter;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;

import java.util.UUID;

/**
 * Base class for {@link ProcessStep} implementations.
 * <p>
 * Provides:
 * <ul>
 *   <li>A fixed {@link ProcessStepType} (usually an enum value)</li>
 *   <li>An auto-generated unique {@link UUID} step identifier used for correlation/monitoring</li>
 * </ul>
 * <p>
 * Step implementations should extend this class when they only need to provide business logic in
 * {@link #execute(org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext)}.
 *
 * @param <C> the concrete {@link ProcessConfig} type required by this step
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public abstract class AbstractProcessStep<C extends ProcessConfig> implements ProcessStep<C> {

    /**
     * Functional classification of the step.
     */
    private final ProcessStepType type;

    /**
     * Unique identifier of this step instance.
     */
    private final UUID id;

    /**
     * Creates a step with the given type and a newly generated identifier.
     *
     * @param type step type (name is typically used in external status updates)
     */
    protected AbstractProcessStep(ProcessStepType type) {
        this.type = type;
        this.id = UUID.randomUUID();
    }
}
