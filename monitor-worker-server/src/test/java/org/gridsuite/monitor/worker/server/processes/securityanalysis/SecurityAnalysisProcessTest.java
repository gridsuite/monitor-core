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
import org.gridsuite.monitor.worker.server.services.FilterService;
import org.gridsuite.monitor.worker.server.services.NetworkConversionService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationService;
import org.gridsuite.monitor.worker.server.services.NotificationService;
import org.gridsuite.monitor.worker.server.services.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class SecurityAnalysisProcessTest {

    @Mock
    private StepExecutionService<SecurityAnalysisConfig> stepExecutionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NetworkConversionService networkConversionService;

    @Mock
    private DummySecurityAnalysisService securityAnalysisService;

    @Mock
    private NetworkModificationService networkModificationService;

    @Mock
    private FilterService filterService;

    private SecurityAnalysisProcess process;

    @BeforeEach
    void setUp() {
        process = new SecurityAnalysisProcess(
            stepExecutionService,
            notificationService,
            networkConversionService,
            securityAnalysisService,
            networkModificationService,
            filterService
        );
    }

    @Test
    void defineStepsShouldReturnThreeStepsInCorrectOrder() {
        List<ProcessStep<SecurityAnalysisConfig>> steps = process.defineSteps();

        assertNotNull(steps);
        assertEquals(3, steps.size());
        assertEquals("LOAD_NETWORK", steps.get(0).getType().getName());
        assertEquals("APPLY_MODIFICATIONS", steps.get(1).getType().getName());
        assertEquals("RUN_SA_COMPUTATION", steps.get(2).getType().getName());
        ProcessType type = process.getProcessType();
        assertEquals(ProcessType.SECURITY_ANALYSIS, type);
    }
}
