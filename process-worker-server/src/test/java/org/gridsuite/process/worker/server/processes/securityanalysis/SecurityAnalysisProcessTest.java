/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.processes.securityanalysis;

import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.core.ProcessStep;
import org.gridsuite.process.worker.server.services.NetworkConversionService;
import org.gridsuite.process.worker.server.services.NotificationService;
import org.gridsuite.process.worker.server.services.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    private SecurityAnalysisProcess process;

    @BeforeEach
    void setUp() {
        process = new SecurityAnalysisProcess(
            stepExecutionService,
            notificationService,
            networkConversionService,
            securityAnalysisService
        );
    }

    @Test
    void defineStepsShouldReturnThreeStepsInCorrectOrder() {
        // When
        List<ProcessStep<SecurityAnalysisConfig>> steps = process.defineSteps();

        // Then
        assertNotNull(steps);
        assertEquals(3, steps.size());

        // Verify step order: LOAD_NETWORK -> APPLY_MODIFICATIONS -> RUN_COMPUTATION
        assertEquals("LOAD_NETWORK", steps.get(0).getType().getName());
        assertEquals("APPLY_MODIFICATIONS", steps.get(1).getType().getName());
        assertEquals("RUN_SA_COMPUTATION", steps.get(2).getType().getName());
        ProcessType type = process.getProcessType();
        assertEquals(ProcessType.SECURITY_ANALYSIS, type);
    }
}
