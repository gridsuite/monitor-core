package org.gridsuite.monitor.server.testdata.processconfig;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class SecurityAnalysisConfigTestDataProvider implements ProcessConfigTestDataProvider<SecurityAnalysisConfig, SecurityAnalysisConfigEntity> {

    @Override
    public SecurityAnalysisConfig createDto() {
        return ProcessConfigTestDataFactory.securityAnalysisDto();
    }

    @Override
    public SecurityAnalysisConfigEntity createEntity() {
        return ProcessConfigTestDataFactory.securityAnalysisEntity();
    }

    @Override
    public Class<SecurityAnalysisConfigEntity> entityType() {
        return SecurityAnalysisConfigEntity.class;
    }

    @Override
    public Class<SecurityAnalysisConfig> dtoType() {
        return SecurityAnalysisConfig.class;
    }

    @Override
    public void assertDtoEntityEquivalent(SecurityAnalysisConfig dto, SecurityAnalysisConfigEntity entity) {
        ProcessConfigTestDataProvider.super.assertDtoEntityEquivalent(dto, entity);
        assertThat(entity.getLoadflowParametersUuid())
                .isEqualTo(dto.loadflowParametersUuid());
        assertThat(entity.getSecurityAnalysisParametersUuid())
                .isEqualTo(dto.securityAnalysisParametersUuid());
    }
}
