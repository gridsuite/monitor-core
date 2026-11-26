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
public enum StepStatus {
    SCHEDULED,
    RUNNING,
    COMPLETED,
    FAILED,
    SKIPPED,
    CANCELED;

    private static final Map<StepStatus, Set<StepStatus>> ALLOWED_TRANSITIONS = Map.of(
        SCHEDULED, Set.of(RUNNING, SKIPPED, CANCELED),
        RUNNING, Set.of(COMPLETED, FAILED, CANCELED)
    );

    /**
     * @return true if moving from this status to the target status is an allowed transition.
     * A status without any allowed transition is a final status.
     */
    public boolean canTransitionTo(StepStatus target) {
        return this == target || ALLOWED_TRANSITIONS
            .getOrDefault(this, Set.of())
            .contains(target);
    }
}
