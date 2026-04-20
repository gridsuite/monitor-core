/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processexecution;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Schema(enumAsRef = true)
public enum ProcessStatus {
    SCHEDULED,
    RUNNING,
    COMPLETED,
    FAILED
}
