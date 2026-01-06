package org.gridsuite.process.worker.server.processes.securityanalysis.steps;

import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.core.AbstractProcessStep;
import org.gridsuite.process.worker.server.processes.securityanalysis.SAStepTypes;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;

import java.util.List;
import java.util.UUID;

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
