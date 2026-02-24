/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.context;

import com.powsybl.iidm.network.Network;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public class ProcessExecutionContext<C extends ProcessConfig> {

    private final UUID executionId;
    private final UUID caseUuid;
    private final C config;
    @Setter
    private Network network;
    private final String executionEnvName;

    public ProcessExecutionContext(UUID executionId, UUID caseUuid, C config, String executionEnvName) {
        this.executionId = executionId;
        this.caseUuid = caseUuid;
        this.config = config;
        this.executionEnvName = executionEnvName;
    }

    public ProcessStepExecutionContext<C> createStepContext(ProcessStep<? super C> step, int stepOrder) {
        return new ProcessStepExecutionContext<>(this, step.getType(), step.getId(), stepOrder);
    }
}
