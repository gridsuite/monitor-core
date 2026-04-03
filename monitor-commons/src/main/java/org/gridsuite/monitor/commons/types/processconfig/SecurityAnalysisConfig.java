/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public record SecurityAnalysisConfig(
    UUID securityAnalysisParametersUuid,
    List<UUID> modificationUuids,
    UUID loadflowParametersUuid
) implements ProcessConfig {
    @Override
    public ProcessType processType() {
        return ProcessType.SECURITY_ANALYSIS;
    }

    @Override
    public List<ProcessConfigFieldComparison> compareWith(ProcessConfig other) {
        SecurityAnalysisConfig o = (SecurityAnalysisConfig) other;
        return List.of(
            new ProcessConfigFieldComparison("modifications",
                Objects.equals(modificationUuids, o.modificationUuids),
                modificationUuids, o.modificationUuids),
            new ProcessConfigFieldComparison("securityAnalysisParameters",
                Objects.equals(securityAnalysisParametersUuid, o.securityAnalysisParametersUuid),
                securityAnalysisParametersUuid, o.securityAnalysisParametersUuid),
            new ProcessConfigFieldComparison("loadflowParameters",
                Objects.equals(loadflowParametersUuid, o.loadflowParametersUuid),
                loadflowParametersUuid, o.loadflowParametersUuid)
        );
    }
}
