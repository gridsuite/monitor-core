/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.dto.automaticprocessconfig;

import jakarta.validation.constraints.NotNull;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

import java.util.UUID;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
public record AutomaticProcessConfigRequest(
    @NotNull ProcessType processType,
    @NotNull UUID configUuid
) {
}
