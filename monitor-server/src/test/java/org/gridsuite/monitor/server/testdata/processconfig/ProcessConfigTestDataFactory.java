package org.gridsuite.monitor.server.testdata.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProcessConfigTestDataFactory {

    private ProcessConfigTestDataFactory() {
    }

    public static List<UUID> modifications(boolean immutable) {
        List<UUID> modifications = List.of(UUID.randomUUID(), UUID.randomUUID());
        return immutable ? modifications : new ArrayList<>(modifications);
    }

    public static LoadFlowConfigEntity loadFlowEntity() {
        LoadFlowConfigEntity e = new LoadFlowConfigEntity();
        e.setProcessType(ProcessType.LOADFLOW);
        e.setModificationUuids(modifications(false));
        e.setLoadflowParametersUuid(UUID.randomUUID());
        return e;
    }

    public static LoadFlowConfig loadFlowDto() {
        return new LoadFlowConfig(UUID.randomUUID(), modifications(true));
    }

    public static SecurityAnalysisConfigEntity securityAnalysisEntity() {
        SecurityAnalysisConfigEntity e = new SecurityAnalysisConfigEntity();
        e.setProcessType(ProcessType.SECURITY_ANALYSIS);
        e.setModificationUuids(modifications(false));
        e.setSecurityAnalysisParametersUuid(UUID.randomUUID());
        e.setLoadflowParametersUuid(UUID.randomUUID());
        return e;
    }

    public static SecurityAnalysisConfig securityAnalysisDto() {
        return new SecurityAnalysisConfig(UUID.randomUUID(), modifications(true), UUID.randomUUID());
    }
}
