/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core;

import lombok.Getter;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.utils.S3PathUtils;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
public abstract class AbstractProcessStep<C extends ProcessConfig> implements ProcessStep<C> {

    private final ProcessStepType type;
    private final UUID id;

    protected AbstractProcessStep(ProcessStepType type) {
        this.type = type;
        this.id = UUID.randomUUID();
    }

    protected String getDebugFilePath(ProcessStepExecutionContext<C> context, String fileName) {
        return String.join(S3PathUtils.S3_DELIMITER,
            S3PathUtils.toDebugLocation(context.getExecutionEnvironment(), context.getConfig().processType().name(), context.getProcessExecutionId()),
            type.getName() + "_" + context.getStepOrder(),
            fileName);
    }
}
