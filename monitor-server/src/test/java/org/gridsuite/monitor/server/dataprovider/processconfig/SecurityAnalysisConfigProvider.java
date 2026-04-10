package org.gridsuite.monitor.server.dataprovider.processconfig;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class SecurityAnalysisConfigProvider implements ProcessConfigProvider<SecurityAnalysisConfig, SecurityAnalysisConfigEntity> {

    @Override
    public SecurityAnalysisConfig createDto() {
        return ProcessConfigTestDataFactory.securityAnalysisDto();
    }

    @Override
    public SecurityAnalysisConfig updateDto(SecurityAnalysisConfig dto) {
        return new SecurityAnalysisConfig(UUID.randomUUID(), dto.modificationUuids(), UUID.randomUUID());
    }

    @Override
    public SecurityAnalysisConfigEntity createEntity() {
        return ProcessConfigTestDataFactory.securityAnalysisEntity();
    }

    @Override
    public SecurityAnalysisConfigEntity updateEntity(SecurityAnalysisConfigEntity entity) {
        entity.setLoadflowParametersUuid(UUID.randomUUID());
        entity.setSecurityAnalysisParametersUuid(UUID.randomUUID());
        return entity;
    }

    @Override
    public Class<SecurityAnalysisConfigEntity> entityType() {
        return SecurityAnalysisConfigEntity.class;
    }

    @Override
    public UUID getId(SecurityAnalysisConfigEntity entity) {
        return entity.getId();
    }

    @Override
    public void assertEntityEquals(SecurityAnalysisConfigEntity expected, SecurityAnalysisConfigEntity actual) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Override
    public void assertDtoEquals(SecurityAnalysisConfig expected, SecurityAnalysisConfig actual) {
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Override
    public void assertDtoEntityEquivalent(SecurityAnalysisConfig dto, SecurityAnalysisConfigEntity entity) {
        assertThat(entity.getLoadflowParametersUuid())
                .isEqualTo(dto.loadflowParametersUuid());
        assertThat(entity.getSecurityAnalysisParametersUuid())
                .isEqualTo(dto.securityAnalysisParametersUuid());
        assertThat(entity.getModificationUuids())
                .isEqualTo(dto.modificationUuids());
    }
}
