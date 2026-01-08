/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis.steps;

import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.SAStepTypes;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class SAApplyModificationsStep extends AbstractProcessStep<SecurityAnalysisConfig> {

    public SAApplyModificationsStep() {
        super(SAStepTypes.APPLY_MODIFICATIONS);
    }

    @Override
    public void execute(ProcessStepExecutionContext<SecurityAnalysisConfig> context) {
        List<UUID> modificationIds = context.getConfig().modificationUuids();
        if (modificationIds != null && !modificationIds.isEmpty()) {
            applyModifications(modificationIds);
        }
    }

    private void applyModifications(List<UUID> modificationIds) {
        //TODO
        System.out.println("Applying modifications: " + modificationIds);
    }
}
