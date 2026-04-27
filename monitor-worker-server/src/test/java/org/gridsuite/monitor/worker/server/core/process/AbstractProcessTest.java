/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractProcessTest<C extends ProcessConfig, T extends AbstractProcess<C>> {

    protected T process;

    @BeforeEach
    protected abstract void setUp();

    protected abstract List<ProcessStep<C>> getExpectedSteps();

    protected abstract ProcessType getExpectedProcessType();

    @Test
    void getProcessTypeShouldReturnProcessType() {
        assertEquals(getExpectedProcessType(), process.getProcessType());
    }

    @Test
    void defineStepsShouldReturnThreeStepsInCorrectOrder() {
        List<ProcessStep<C>> steps = process.defineSteps();

        assertNotNull(steps);
        assertEquals(getExpectedSteps().size(), steps.size());
        for (int i = 0; i < steps.size(); i++) {
            assertSame(getExpectedSteps().get(i), steps.get(i));
        }
    }
}
