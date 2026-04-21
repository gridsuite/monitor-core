/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.loadflow;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.gridsuite.monitor.worker.server.process.commons.steps.ApplyModificationsStep;
import org.gridsuite.monitor.worker.server.process.commons.steps.LoadNetworkStep;
import org.gridsuite.monitor.worker.server.process.loadflow.steps.LoadflowRunComputationStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
 class LoadflowProcessTest {

    @Mock
    private LoadNetworkStep<LoadFlowConfig> loadNetworkStep;

    @Mock
    private ApplyModificationsStep<LoadFlowConfig> applyModificationsStep;

    @Mock
    private LoadflowRunComputationStep runComputationStep;

    private LoadflowProcess process;

    @BeforeEach
    void setUp() {
        process = new LoadflowProcess(
            loadNetworkStep,
            applyModificationsStep,
            runComputationStep
        );
    }

    @Test
    void getProcessTypeShouldReturnLoadflow() {
        assertEquals(ProcessType.LOADFLOW, process.getProcessType());
    }

    @Test
    void defineStepsShouldReturnThreeStepsInCorrectOrder() {
        List<ProcessStep<LoadFlowConfig>> steps = process.defineSteps();

        assertNotNull(steps);
        assertEquals(3, steps.size());
        assertSame(loadNetworkStep, steps.get(0));
        assertSame(applyModificationsStep, steps.get(1));
        assertSame(runComputationStep, steps.get(2));
    }
}
