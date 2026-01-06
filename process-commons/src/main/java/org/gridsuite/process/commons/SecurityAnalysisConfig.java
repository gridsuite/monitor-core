package org.gridsuite.process.commons;

import java.util.List;
import java.util.UUID;

public record SecurityAnalysisConfig(
    UUID caseUuid,
    UUID executionId,
    UUID parametersUuid,
    List<String> contingencies,
    List<UUID> modificationUuids
) implements ProcessConfig {

    @Override
    public ProcessType processType() {
        return ProcessType.SECURITY_ANALYSIS;
    }

    @Override
    public ProcessConfig withExecutionId(UUID executionId) {
        return new SecurityAnalysisConfig(caseUuid, executionId, parametersUuid, contingencies, modificationUuids);
    }
}
