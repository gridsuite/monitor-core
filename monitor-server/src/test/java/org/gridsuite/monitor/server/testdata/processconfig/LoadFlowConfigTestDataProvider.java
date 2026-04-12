package org.gridsuite.monitor.server.testdata.processconfig;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class LoadFlowConfigTestDataProvider implements ProcessConfigTestDataProvider<LoadFlowConfig, LoadFlowConfigEntity> {

    @Override
    public LoadFlowConfig createDto() {
        return ProcessConfigTestDataFactory.loadFlowDto();
    }

    @Override
    public LoadFlowConfigEntity createEntity() {
        return ProcessConfigTestDataFactory.loadFlowEntity();
    }

    @Override
    public Class<LoadFlowConfigEntity> entityType() {
        return LoadFlowConfigEntity.class;
    }

    @Override
    public Class<LoadFlowConfig> dtoType() {
        return LoadFlowConfig.class;
    }

    @Override
    public void assertDtoEntityEquivalent(LoadFlowConfig dto, LoadFlowConfigEntity entity) {
        ProcessConfigTestDataProvider.super.assertDtoEntityEquivalent(dto, entity);
        assertThat(entity.getLoadflowParametersUuid())
                .isEqualTo(dto.loadflowParametersUuid());
    }
}
