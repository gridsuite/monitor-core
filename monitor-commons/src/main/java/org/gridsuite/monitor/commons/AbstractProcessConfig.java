/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
public abstract class AbstractProcessConfig implements ProcessConfig {
    @JsonProperty(required = true)
    private final List<UUID> modificationUuids;

    private final String owner;

    private final Instant creationDate;

    private final Instant lastModificationDate;

    private final String lastModifiedBy;

    protected AbstractProcessConfig(
        List<UUID> modificationUuids,
        String owner,
        Instant creationDate,
        Instant lastModificationDate,
        String lastModifiedBy
    ) {
        this.modificationUuids = modificationUuids != null ? List.copyOf(modificationUuids) : null;
        this.owner = owner;
        this.creationDate = creationDate;
        this.lastModificationDate = lastModificationDate;
        this.lastModifiedBy = lastModifiedBy;
    }
}
