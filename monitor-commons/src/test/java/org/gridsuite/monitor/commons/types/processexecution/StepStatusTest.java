/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processexecution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.monitor.commons.types.processexecution.StepStatus.*;

class StepStatusTest {
    @ParameterizedTest
    @EnumSource(StepStatus.class)
    void shouldAllowSameStatusTransition(StepStatus status) {
        assertThat(status.canTransitionTo(status)).isTrue();
    }

    @Test
    void shouldAllowValidTransitions() {
        assertThat(SCHEDULED.canTransitionTo(RUNNING)).isTrue();
        assertThat(SCHEDULED.canTransitionTo(SKIPPED)).isTrue();
        assertThat(SCHEDULED.canTransitionTo(CANCELED)).isTrue();

        assertThat(RUNNING.canTransitionTo(COMPLETED)).isTrue();
        assertThat(RUNNING.canTransitionTo(FAILED)).isTrue();
        assertThat(RUNNING.canTransitionTo(CANCELED)).isTrue();
    }

    @Test
    void shouldRejectInvalidTransitions() {
        assertThat(SCHEDULED.canTransitionTo(COMPLETED)).isFalse();
        assertThat(RUNNING.canTransitionTo(SCHEDULED)).isFalse();

        for (StepStatus status : List.of(COMPLETED, FAILED, SKIPPED, CANCELED)) {
            assertThat(status.canTransitionTo(RUNNING)).isFalse();
        }
    }
}
