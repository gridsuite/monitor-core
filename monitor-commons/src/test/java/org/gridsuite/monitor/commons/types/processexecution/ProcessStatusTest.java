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
import static org.gridsuite.monitor.commons.types.processexecution.ProcessStatus.*;

class ProcessStatusTest {
    @ParameterizedTest
    @EnumSource(ProcessStatus.class)
    void shouldAllowSameStatusTransition(ProcessStatus status) {
        assertThat(status.canTransitionTo(status)).isTrue();
    }

    @Test
    void shouldAllowValidTransitions() {
        assertThat(SCHEDULED.canTransitionTo(RUNNING)).isTrue();
        assertThat(SCHEDULED.canTransitionTo(FAILED)).isTrue();
        assertThat(SCHEDULED.canTransitionTo(CANCELED)).isTrue();

        assertThat(RUNNING.canTransitionTo(COMPLETED)).isTrue();
        assertThat(RUNNING.canTransitionTo(FAILED)).isTrue();
        assertThat(RUNNING.canTransitionTo(CANCELING)).isTrue();
        assertThat(RUNNING.canTransitionTo(CANCELED)).isTrue();

        assertThat(CANCELING.canTransitionTo(CANCELED)).isTrue();
    }

    @Test
    void shouldRejectInvalidTransitions() {
        assertThat(SCHEDULED.canTransitionTo(COMPLETED)).isFalse();
        assertThat(SCHEDULED.canTransitionTo(CANCELING)).isFalse();
        assertThat(RUNNING.canTransitionTo(SCHEDULED)).isFalse();
        assertThat(CANCELING.canTransitionTo(RUNNING)).isFalse();
        assertThat(CANCELING.canTransitionTo(COMPLETED)).isFalse();

        for (ProcessStatus status : List.of(COMPLETED, FAILED, CANCELED)) {
            assertThat(status.canTransitionTo(RUNNING)).isFalse();
        }
    }
}
