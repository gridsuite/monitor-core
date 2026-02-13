/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class SecurityAnalysisConfig extends AbstractProcessConfig {
    @JsonProperty(required = true)
    private final UUID parametersUuid;

    @JsonProperty(required = true)
    private final List<String> contingencies;

    public SecurityAnalysisConfig(
        UUID parametersUuid,
        List<String> contingencies,
        List<UUID> modificationUuids,
        String owner,
        Instant creationDate,
        Instant lastModificationDate,
        String lastModifiedBy
    ) {
        super(modificationUuids, owner, creationDate, lastModificationDate, lastModifiedBy);
        this.parametersUuid = parametersUuid;
        this.contingencies = contingencies != null ? List.copyOf(contingencies) : null;
    }

    @Override
    public ProcessType processType() {
        return ProcessType.SECURITY_ANALYSIS;
    }
}

