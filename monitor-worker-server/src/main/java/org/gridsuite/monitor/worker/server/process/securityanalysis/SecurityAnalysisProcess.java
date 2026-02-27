/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.securityanalysis;

import org.gridsuite.monitor.commons.api.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;
import org.gridsuite.monitor.worker.server.core.process.AbstractProcess;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.gridsuite.monitor.worker.server.process.commons.steps.ApplyModificationsStep;
import org.gridsuite.monitor.worker.server.process.commons.steps.LoadNetworkStep;
import org.gridsuite.monitor.worker.server.process.securityanalysis.steps.SecurityAnalysisRunComputationStep;
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
            LoadNetworkStep<SecurityAnalysisConfig> loadNetworkStep,
            ApplyModificationsStep<SecurityAnalysisConfig> applyModificationsStep,
            SecurityAnalysisRunComputationStep runComputationStep) {
        super(ProcessType.SECURITY_ANALYSIS);
        this.loadNetworkStep = loadNetworkStep;
        this.applyModificationsStep = applyModificationsStep;
        this.runComputationStep = runComputationStep;
    }

    @Override
    protected List<ProcessStep<SecurityAnalysisConfig>> defineSteps() {
        return List.of(
            loadNetworkStep,
            applyModificationsStep,
            runComputationStep
        );
    }
}
