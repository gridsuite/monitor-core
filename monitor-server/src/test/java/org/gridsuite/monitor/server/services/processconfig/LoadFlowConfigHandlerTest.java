/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
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
class LoadFlowConfigHandlerTest extends AbstractProcessConfigHandlerTest<LoadFlowConfig, LoadFlowConfigEntity, LoadFlowConfigMapper, LoadFlowConfigHandler> {
    @Override
    ProcessType getProcessType() {
        return ProcessType.LOADFLOW;
    }

    @Override
    LoadFlowConfig createProcessConfig() {
        return new LoadFlowConfig(UUID.randomUUID(), List.of(UUID.randomUUID()));
    }

    @Override
    LoadFlowConfigEntity createProcessConfigEntity() {
        return new LoadFlowConfigEntity();
    }

    @Override
    @BeforeEach
    protected void setUp() {
        LoadFlowConfigMapper realMapper = Mappers.getMapper(LoadFlowConfigMapper.class);
        mapper = Mockito.spy(realMapper);
        handler = new LoadFlowConfigHandler(mapper);
    }

    @Test
    void computeDifferencesShouldReturnNoDifferenceWhenConfigsAreEqual() {
        LoadFlowConfig processConfig = createProcessConfig();
        LoadFlowConfigEntity entity1 = createProcessConfigEntity();
        LoadFlowConfigEntity entity2 = createProcessConfigEntity();

        when(mapper.toDto(entity1)).thenReturn(processConfig);
        when(mapper.toDto(entity2)).thenReturn(processConfig);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result)
            .hasSize(2)
            .allMatch(ProcessConfigFieldComparison::identical)
            .allMatch(fieldComparison -> fieldComparison.value1().equals(fieldComparison.value2()));
    }

    @Test
    void computeDifferencesShouldReturnDifferentModificationsWhenModificationsAreDifferent() {
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> modificationUuids2 = List.of(UUID.randomUUID(), UUID.randomUUID());

        LoadFlowConfig processConfig1 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids1);
        LoadFlowConfig processConfig2 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids2);

        LoadFlowConfigEntity entity1 = Mockito.mock(LoadFlowConfigEntity.class);
        LoadFlowConfigEntity entity2 = Mockito.mock(LoadFlowConfigEntity.class);

        when(mapper.toDto(entity1)).thenReturn(processConfig1);
        when(mapper.toDto(entity2)).thenReturn(processConfig2);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result).hasSize(2);
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
        UUID loadflowParametersUuid = UUID.randomUUID();
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(mod1, mod2);
        List<UUID> modificationUuids2 = List.of(mod2, mod1); // Different order

        LoadFlowConfig processConfig1 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids1);
        LoadFlowConfig processConfig2 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids2);

        LoadFlowConfigEntity entity1 = Mockito.mock(LoadFlowConfigEntity.class);
        LoadFlowConfigEntity entity2 = Mockito.mock(LoadFlowConfigEntity.class);

        when(mapper.toDto(entity1)).thenReturn(processConfig1);
        when(mapper.toDto(entity2)).thenReturn(processConfig2);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result).hasSize(2);
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
    void computeDifferencesShouldReturnDifferencesWhenParametersAreDifferent() {
        UUID loadflowParametersUuid1 = UUID.randomUUID();
        UUID loadflowParametersUuid2 = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());

        LoadFlowConfig processConfig1 = new LoadFlowConfig(loadflowParametersUuid1, modificationUuids);
        LoadFlowConfig processConfig2 = new LoadFlowConfig(loadflowParametersUuid2, modificationUuids);

        LoadFlowConfigEntity entity1 = Mockito.mock(LoadFlowConfigEntity.class);
        LoadFlowConfigEntity entity2 = Mockito.mock(LoadFlowConfigEntity.class);

        when(mapper.toDto(entity1)).thenReturn(processConfig1);
        when(mapper.toDto(entity2)).thenReturn(processConfig2);

        List<ProcessConfigFieldComparison> result = handler.computeDifferences(entity1, entity2);

        assertThat(result).hasSize(2);
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
