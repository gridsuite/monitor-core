/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
class SecurityAnalysisConfigHandlerTest extends AbstractProcessConfigHandlerTest<SecurityAnalysisConfig, SecurityAnalysisConfigEntity, SecurityAnalysisConfigMapper, SecurityAnalysisConfigHandler> {
    @Override
    ProcessType getProcessType() {
        return ProcessType.SECURITY_ANALYSIS;
    }

    @Override
    SecurityAnalysisConfig createProcessConfig() {
        return new SecurityAnalysisConfig(UUID.randomUUID(), List.of(UUID.randomUUID()), UUID.randomUUID());
    }

    @Override
    SecurityAnalysisConfigEntity createProcessConfigEntity() {
        return new SecurityAnalysisConfigEntity();
    }

    @Override
    @BeforeEach
    protected void setUp() {
        SecurityAnalysisConfigMapper realMapper = Mappers.getMapper(SecurityAnalysisConfigMapper.class);
        mapper = Mockito.spy(realMapper);
        handler = new SecurityAnalysisConfigHandler(mapper);
    }

    @Test
    void computeDifferencesShouldReturnNoDifferenceWhenConfigsAreEqual() {
        SecurityAnalysisConfig processConfig = createProcessConfig();
        SecurityAnalysisConfigEntity entity1 = createProcessConfigEntity();
        SecurityAnalysisConfigEntity entity2 = createProcessConfigEntity();

        when(mapper.toDto(entity1)).thenReturn(processConfig);
        when(mapper.toDto(entity2)).thenReturn(processConfig);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result)
            .hasSize(3)
            .allMatch(ProcessConfigFieldComparison::identical)
            .allMatch(fieldComparison -> fieldComparison.value1().equals(fieldComparison.value2()));
    }

    @Test
    void computeDifferencesShouldReturnDifferentModificationsWhenModificationsAreDifferent() {
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> modificationUuids2 = List.of(UUID.randomUUID(), UUID.randomUUID());
        UUID loadflowParametersUuid = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modificationUuids1, loadflowParametersUuid);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modificationUuids2, loadflowParametersUuid);

        SecurityAnalysisConfigEntity entity1 = Mockito.mock(SecurityAnalysisConfigEntity.class);
        SecurityAnalysisConfigEntity entity2 = Mockito.mock(SecurityAnalysisConfigEntity.class);

        when(mapper.toDto(entity1)).thenReturn(processConfig1);
        when(mapper.toDto(entity2)).thenReturn(processConfig2);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result).hasSize(3);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modificationUuids1);
        assertThat(comparison.value2()).isEqualTo(modificationUuids2);
        verify(mapper).toDto(entity1);
        verify(mapper).toDto(entity2);
    }

    @Test
    void computeDifferencesShouldDetectOrderDifferenceInModifications() {
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(mod1, mod2);
        List<UUID> modificationUuids2 = List.of(mod2, mod1); // Different order
        UUID loadflowParametersUuid = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modificationUuids1, loadflowParametersUuid);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modificationUuids2, loadflowParametersUuid);

        SecurityAnalysisConfigEntity entity1 = Mockito.mock(SecurityAnalysisConfigEntity.class);
        SecurityAnalysisConfigEntity entity2 = Mockito.mock(SecurityAnalysisConfigEntity.class);

        when(mapper.toDto(entity1)).thenReturn(processConfig1);
        when(mapper.toDto(entity2)).thenReturn(processConfig2);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result).hasSize(3);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modificationUuids1);
        assertThat(comparison.value2()).isEqualTo(modificationUuids2);
        verify(mapper).toDto(entity1);
        verify(mapper).toDto(entity2);
    }

    @Test
    void computeDifferencesShouldReturnDifferencesWhenSecurityAnalysisParametersAreDifferent() {
        UUID securityAnalysisParametersUuid1 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid2 = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
        UUID loadflowParametersUuid = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid1, modificationUuids, loadflowParametersUuid);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid2, modificationUuids, loadflowParametersUuid);

        SecurityAnalysisConfigEntity entity1 = Mockito.mock(SecurityAnalysisConfigEntity.class);
        SecurityAnalysisConfigEntity entity2 = Mockito.mock(SecurityAnalysisConfigEntity.class);

        when(mapper.toDto(entity1)).thenReturn(processConfig1);
        when(mapper.toDto(entity2)).thenReturn(processConfig2);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result).hasSize(3);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "securityAnalysisParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(securityAnalysisParametersUuid1);
        assertThat(comparison.value2()).isEqualTo(securityAnalysisParametersUuid2);
        verify(mapper).toDto(entity1);
        verify(mapper).toDto(entity2);
    }

    @Test
    void computeDifferencesShouldReturnDifferencesWhenLoadflowParametersAreDifferent() {
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
        UUID loadflowParametersUuid1 = UUID.randomUUID();
        UUID loadflowParametersUuid2 = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modificationUuids, loadflowParametersUuid1);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modificationUuids, loadflowParametersUuid2);

        SecurityAnalysisConfigEntity entity1 = Mockito.mock(SecurityAnalysisConfigEntity.class);
        SecurityAnalysisConfigEntity entity2 = Mockito.mock(SecurityAnalysisConfigEntity.class);

        when(mapper.toDto(entity1)).thenReturn(processConfig1);
        when(mapper.toDto(entity2)).thenReturn(processConfig2);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result).hasSize(3);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "loadflowParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(loadflowParametersUuid1);
        assertThat(comparison.value2()).isEqualTo(loadflowParametersUuid2);
        verify(mapper).toDto(entity1);
        verify(mapper).toDto(entity2);
    }
}
