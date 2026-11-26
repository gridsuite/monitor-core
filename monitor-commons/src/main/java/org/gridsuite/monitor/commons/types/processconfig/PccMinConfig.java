/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processconfig;

import jakarta.validation.constraints.NotNull;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public record PccMinConfig(
    @NotNull
    UUID shortCircuitParametersUuid,
    @NotNull
    UUID pccMinParametersUuid,
    @NotNull
    List<ModificationInfo> modifications,
    @NotNull
    List<UUID> watchedGenerators
) implements ProcessConfig {
    @Override
    public ProcessType processType() {
        return ProcessType.PCC_MIN;
    }

    @Override
    public List<ProcessConfigFieldComparison> compareWith(ProcessConfig other) {
        PccMinConfig o = (PccMinConfig) other;
        return List.of(
            new ProcessConfigFieldComparison("modifications",
                Objects.equals(this.modifications, o.modifications),
                this.modifications, o.modifications),
            new ProcessConfigFieldComparison("shortCircuitParameters",
                Objects.equals(this.shortCircuitParametersUuid, o.shortCircuitParametersUuid),
                this.shortCircuitParametersUuid, o.shortCircuitParametersUuid),
            new ProcessConfigFieldComparison("pccMinParameters",
                Objects.equals(this.pccMinParametersUuid, o.pccMinParametersUuid),
                this.pccMinParametersUuid, o.pccMinParametersUuid),
            new ProcessConfigFieldComparison("watchedGenerators",
                Objects.equals(this.watchedGenerators, o.watchedGenerators),
                this.watchedGenerators, o.watchedGenerators)
        );
    }
}
