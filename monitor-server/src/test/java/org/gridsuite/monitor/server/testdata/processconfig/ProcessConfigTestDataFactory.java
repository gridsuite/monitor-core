package org.gridsuite.monitor.server.testdata.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;

import java.util.List;
import java.util.UUID;

public final class ProcessConfigTestDataFactory {

    private ProcessConfigTestDataFactory() {
    }

    public static List<UUID> modifications() {
        return List.of(UUID.randomUUID(), UUID.randomUUID());
    }

    public static LoadFlowConfigEntity loadFlowEntity() {
        LoadFlowConfigEntity e = new LoadFlowConfigEntity();
        e.setModificationUuids(modifications());
        e.setLoadflowParametersUuid(UUID.randomUUID());
        return e;
    }

    public static LoadFlowConfig loadFlowDto() {
        return new LoadFlowConfig(UUID.randomUUID(), modifications());
    }

    public static SecurityAnalysisConfigEntity securityAnalysisEntity() {
        SecurityAnalysisConfigEntity e = new SecurityAnalysisConfigEntity();
        e.setModificationUuids(modifications());
        e.setSecurityAnalysisParametersUuid(UUID.randomUUID());
        e.setLoadflowParametersUuid(UUID.randomUUID());
        return e;
    }

    public static SecurityAnalysisConfig securityAnalysisDto() {
        return new SecurityAnalysisConfig(UUID.randomUUID(), modifications(), UUID.randomUUID());
    }
}
