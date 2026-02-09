/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis;

import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.processes.commons.steps.ApplyModificationsStep;
import org.gridsuite.monitor.worker.server.processes.commons.steps.LoadNetworkStep;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.steps.SecurityAnalysisRunComputationStep;
import org.gridsuite.monitor.worker.server.services.internal.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class SecurityAnalysisProcessTest {

    @Mock
    private StepExecutionService<SecurityAnalysisConfig> stepExecutionService;

    @Mock
    private LoadNetworkStep<SecurityAnalysisConfig> loadNetworkStep;

    @Mock
    private ApplyModificationsStep<SecurityAnalysisConfig> applyModificationsStep;

    @Mock
    private SecurityAnalysisRunComputationStep runComputationStep;

    private SecurityAnalysisProcess process;

    @BeforeEach
    void setUp() {
        process = new SecurityAnalysisProcess(
            stepExecutionService,
            loadNetworkStep,
            applyModificationsStep,
            runComputationStep
        );
    }

    @Test
    void getProcessTypeShouldReturnSecurityAnalysis() {
        assertEquals(ProcessType.SECURITY_ANALYSIS, process.getProcessType());
    }

    @Test
    void defineStepsShouldReturnThreeStepsInCorrectOrder() {
        List<ProcessStep<SecurityAnalysisConfig>> steps = process.defineSteps();

        assertNotNull(steps);
        assertEquals(3, steps.size());
        assertSame(loadNetworkStep, steps.get(0));
        assertSame(applyModificationsStep, steps.get(1));
        assertSame(runComputationStep, steps.get(2));
    }
}
