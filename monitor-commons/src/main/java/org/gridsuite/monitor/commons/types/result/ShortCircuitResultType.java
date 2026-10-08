/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, software distributed under the terms of the Mozilla Public
 * License is distributed on an "AS IS" BASIS.
 */
package org.gridsuite.monitor.commons.types.result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum ShortCircuitResultType {
    ALL_BUSES,
    ONE_BUS
}
