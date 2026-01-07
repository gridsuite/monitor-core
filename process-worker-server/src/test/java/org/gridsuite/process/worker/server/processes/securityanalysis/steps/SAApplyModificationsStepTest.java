/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.processes.securityanalysis.steps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SAApplyModificationsStepTest {

    private SAApplyModificationsStep applyModificationsStep;

    @BeforeEach
    void setUp() {
        applyModificationsStep = new SAApplyModificationsStep();
    }

    @Test
    void getTypeShouldReturnApplyModifications() {
        String stepType = applyModificationsStep.getType().getName();

        assertEquals("APPLY_MODIFICATIONS", stepType);
    }
}
