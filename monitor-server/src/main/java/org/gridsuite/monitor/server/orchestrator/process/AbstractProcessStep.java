/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator.process;

import lombok.Getter;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public abstract class AbstractProcessStep<C extends ProcessConfig> implements ProcessStep<C> {

    private final ProcessStepType type;

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
