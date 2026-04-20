/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.loadflow;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.worker.server.core.process.AbstractProcess;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.gridsuite.monitor.worker.server.process.commons.steps.ApplyModificationsStep;
import org.gridsuite.monitor.worker.server.process.commons.steps.LoadNetworkStep;
import org.gridsuite.monitor.worker.server.process.loadflow.steps.LoadflowRunComputationStep;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Component
public class LoadflowProcess extends AbstractProcess<LoadFlowConfig> {

    private final LoadNetworkStep<LoadFlowConfig> loadNetworkStep;
    private final ApplyModificationsStep<LoadFlowConfig> applyModificationsStep;
    private final LoadflowRunComputationStep runComputationStep;

    public LoadflowProcess(
        LoadNetworkStep<LoadFlowConfig> loadNetworkStep,
        ApplyModificationsStep<LoadFlowConfig> applyModificationsStep,
        LoadflowRunComputationStep runComputationStep) {
        super(ProcessType.LOADFLOW);
        this.loadNetworkStep = loadNetworkStep;
        this.applyModificationsStep = applyModificationsStep;
        this.runComputationStep = runComputationStep;
    }

    @Override
    protected List<ProcessStep<LoadFlowConfig>> defineSteps() {
        return List.of(
            loadNetworkStep,
            applyModificationsStep,
            runComputationStep
        );
    }
}
