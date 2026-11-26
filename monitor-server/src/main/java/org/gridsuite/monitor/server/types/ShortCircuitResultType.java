/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.types;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@Schema(enumAsRef = true)
public enum ShortCircuitResultType {
    ALL_BUSES,
    ONE_BUS
}
