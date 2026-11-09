/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processexecution;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Set;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Schema(enumAsRef = true)
public enum ProcessStatus {
    SCHEDULED,
    RUNNING,
    CANCELING,
    CANCELED,
    COMPLETED,
    FAILED;

    private static final Map<ProcessStatus, Set<ProcessStatus>> ALLOWED_TRANSITIONS = Map.of(
        SCHEDULED, Set.of(RUNNING, FAILED, CANCELED),
        RUNNING, Set.of(COMPLETED, FAILED, CANCELING, CANCELED),
        CANCELING, Set.of(CANCELED)
    );

    /**
     * @return true if moving from this status to the target status is an allowed transition.
     * A status without any allowed transition is a final status.
     */
    public boolean canTransitionTo(ProcessStatus target) {
        return this == target || ALLOWED_TRANSITIONS
            .getOrDefault(this, Set.of())
            .contains(target);
    }
}
