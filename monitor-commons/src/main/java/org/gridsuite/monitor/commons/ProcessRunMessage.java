/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public record ProcessRunMessage<T extends ProcessConfig>(
    UUID executionId,
    UUID caseUuid,
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "processType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = SecurityAnalysisConfig.class, name = "SECURITY_ANALYSIS")
    })
    T config,
    boolean isDebug
) {
    public ProcessType processType() {
        return config.processType();
    }
}
