package org.gridsuite.monitor.server.dataprovider.processconfig;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class LoadFlowConfigProvider implements ProcessConfigProvider<LoadFlowConfig, LoadFlowConfigEntity> {

    @Override
    public LoadFlowConfig createDto() {
        return ProcessConfigTestDataFactory.loadFlowDto();
    }

    @Override
    public LoadFlowConfig updateDto(LoadFlowConfig dto) {
        return new LoadFlowConfig(UUID.randomUUID(), dto.modificationUuids());
    }

    @Override
    public LoadFlowConfigEntity createEntity() {
        return ProcessConfigTestDataFactory.loadFlowEntity();
    }

    @Override
    public LoadFlowConfigEntity updateEntity(LoadFlowConfigEntity entity) {
        entity.setLoadflowParametersUuid(UUID.randomUUID());
        return entity;
    }

    @Override
    public Class<LoadFlowConfigEntity> entityType() {
        return LoadFlowConfigEntity.class;
    }

    @Override
    public UUID getId(LoadFlowConfigEntity entity) {
        return entity.getId();
    }

    @Override
    public void assertEntityEquals(LoadFlowConfigEntity expected, LoadFlowConfigEntity actual) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Override
    public void assertDtoEquals(LoadFlowConfig expected, LoadFlowConfig actual) {
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Override
    public void assertDtoEntityEquivalent(LoadFlowConfig dto, LoadFlowConfigEntity entity) {
        assertThat(entity.getLoadflowParametersUuid())
                .isEqualTo(dto.loadflowParametersUuid());
    }
}
