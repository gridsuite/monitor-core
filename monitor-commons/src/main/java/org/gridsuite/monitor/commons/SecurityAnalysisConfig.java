/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public record SecurityAnalysisConfig(
    UUID id,
    UUID parametersUuid,
    List<String> contingencies,
    List<UUID> modificationUuids
) implements ProcessConfig {
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public ProcessType processType() {
        return ProcessType.SECURITY_ANALYSIS;
    }
}
