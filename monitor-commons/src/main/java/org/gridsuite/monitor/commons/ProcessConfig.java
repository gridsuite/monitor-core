/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "processType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SecurityAnalysisConfig.class, name = "SECURITY_ANALYSIS")
})
public interface ProcessConfig {
    ProcessType processType();

    List<UUID> getModificationUuids();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String getOwner();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Instant getCreationDate();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Instant getLastModificationDate();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String getLastModifiedBy();
}
