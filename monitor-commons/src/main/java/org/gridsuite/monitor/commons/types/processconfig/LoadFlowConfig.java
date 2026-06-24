/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processconfig;

import jakarta.validation.constraints.NotNull;
import org.gridsuite.monitor.commons.error.MonitorException;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.gridsuite.monitor.commons.error.MonitorBusinessErrorCode.DIFFERENT_PROCESS_CONFIG_TYPE;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public record LoadFlowConfig(
    @NotNull
    UUID loadflowParametersUuid,
    @NotNull
    List<UUID> modificationUuids
) implements ProcessConfig {
    @Override
    public ProcessType processType() {
        return ProcessType.LOADFLOW;
    }

    @Override
    public List<ProcessConfigFieldComparison> compareWith(ProcessConfig other) {
        if (!(other instanceof LoadFlowConfig o)) {
            throw new MonitorException(DIFFERENT_PROCESS_CONFIG_TYPE, "Cannot compare different process config types",
                Map.of("processConfigEntity1Type", this.processType(), "processConfigEntity2Type", other.processType()));
        }
        return List.of(
            new ProcessConfigFieldComparison("modifications",
                Objects.equals(this.modificationUuids, o.modificationUuids),
                this.modificationUuids, o.modificationUuids),
            new ProcessConfigFieldComparison("loadflowParameters",
                Objects.equals(this.loadflowParametersUuid, o.loadflowParametersUuid),
                this.loadflowParametersUuid, o.loadflowParametersUuid)
        );
    }
}
