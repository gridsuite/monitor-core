/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ShortCircuitConfigValidatorTest {
    private final ShortCircuitConfigValidator validator = new ShortCircuitConfigValidator();

    @Test
    void getProcessTypeTest() {
        assertThat(validator.getProcessType()).isEqualTo(ProcessType.SHORT_CIRCUIT);
    }

    @Test
    void validateTest() {
        ShortCircuitConfig config = new ShortCircuitConfig(UUID.randomUUID(), List.of());

        assertDoesNotThrow(() -> validator.validate(config));
    }
}
