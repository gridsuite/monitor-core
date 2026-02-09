/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis;

import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcess;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.processes.commons.steps.ApplyModificationsStep;
import org.gridsuite.monitor.worker.server.processes.commons.steps.LoadNetworkStep;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.steps.SecurityAnalysisRunComputationStep;
import org.gridsuite.monitor.worker.server.services.internal.StepExecutionService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class SecurityAnalysisProcess extends AbstractProcess<SecurityAnalysisConfig> {

    private final LoadNetworkStep<SecurityAnalysisConfig> loadNetworkStep;
    private final ApplyModificationsStep<SecurityAnalysisConfig> applyModificationsStep;
    private final SecurityAnalysisRunComputationStep runComputationStep;

    public SecurityAnalysisProcess(
            StepExecutionService<SecurityAnalysisConfig> stepExecutionService,
            LoadNetworkStep<SecurityAnalysisConfig> loadNetworkStep,
            ApplyModificationsStep<SecurityAnalysisConfig> applyModificationsStep,
            SecurityAnalysisRunComputationStep runComputationStep) {
        super(ProcessType.SECURITY_ANALYSIS, stepExecutionService);
        this.loadNetworkStep = loadNetworkStep;
        this.applyModificationsStep = applyModificationsStep;
        this.runComputationStep = runComputationStep;
    }

    @Override
    public List<ProcessStep<SecurityAnalysisConfig>> defineSteps() {
        return List.of(
            loadNetworkStep,
            applyModificationsStep,
            runComputationStep
        );
    }
}
