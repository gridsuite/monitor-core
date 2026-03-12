/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Schema(description = "Field comparison result")
public record ProcessConfigFieldComparison(
    String field,
    boolean identical,
    Object value1,
    Object value2) { }
