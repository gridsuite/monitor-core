/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.server.dto.processconfig.MetadataInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessConfigServiceTest {
    @Mock
    private ProcessConfigRepository processConfigRepository;

    @InjectMocks
    private ProcessConfigService processConfigService;

    private SecurityAnalysisConfig securityAnalysisConfig;
    private LoadFlowConfig loadFlowConfig;

    @Spy
    private final SecurityAnalysisConfigMapper securityAnalysisConfigMapper = Mappers.getMapper(SecurityAnalysisConfigMapper.class);

    @Spy
    private final LoadFlowConfigMapper loadFlowConfigMapper = Mappers.getMapper(LoadFlowConfigMapper.class);

    @BeforeEach
    void setUp() {
        securityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                UUID.randomUUID()
        );
        loadFlowConfig = new LoadFlowConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID())
        );
    }

    @Test
    void createSecurityAnalysisConfig() {
        UUID expectedProcessConfigId = UUID.randomUUID();
        when(processConfigRepository.save(any(SecurityAnalysisConfigEntity.class)))
            .thenAnswer(invocation -> {
                SecurityAnalysisConfigEntity entity = invocation.getArgument(0);
                entity.setId(expectedProcessConfigId);
                return entity;
            });

        UUID result = processConfigService.createProcessConfig(securityAnalysisConfig);
        assertThat(result).isEqualTo(expectedProcessConfigId);

        ArgumentCaptor<SecurityAnalysisConfigEntity> captor = ArgumentCaptor.forClass(SecurityAnalysisConfigEntity.class);
        verify(processConfigRepository).save(captor.capture());

        SecurityAnalysisConfigEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getId()).isEqualTo(expectedProcessConfigId);
        assertThat(savedEntity.getProcessType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        assertThat(savedEntity.getSecurityAnalysisParametersUuid()).isEqualTo(securityAnalysisConfig.securityAnalysisParametersUuid());
        assertThat(savedEntity.getModificationUuids()).isEqualTo(securityAnalysisConfig.modificationUuids());
    }

    @Test
    void getSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = securityAnalysisConfigMapper.toEntity(securityAnalysisConfig);

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(securityAnalysisConfigEntity));

        Optional<PersistedProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isPresent();
        assertThat(processConfig.get().processConfig()).usingRecursiveComparison().isEqualTo(securityAnalysisConfig);
    }

    @Test
    void getProcessConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        Optional<PersistedProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isEmpty();
    }

    @Test
    void getSecurityAnalysisConfigsMetadata() {
        UUID processConfigId1 = UUID.randomUUID();
        UUID processConfigId2 = UUID.randomUUID();

        SecurityAnalysisConfigEntity entity1 = securityAnalysisConfigMapper.toEntity(securityAnalysisConfig);
        entity1.setId(processConfigId1);
        SecurityAnalysisConfigEntity entity2 = securityAnalysisConfigMapper.toEntity(securityAnalysisConfig);
        entity2.setId(processConfigId2);

        when(processConfigRepository.findAllById(List.of(processConfigId1, processConfigId2)))
            .thenReturn(List.of(entity1, entity2));

        List<MetadataInfos> metadataInfos = processConfigService.getProcessConfigsMetadata(List.of(processConfigId1, processConfigId2));

        verify(processConfigRepository).findAllById(List.of(processConfigId1, processConfigId2));
        assertThat(metadataInfos).isEqualTo(List.of(
            new MetadataInfos(processConfigId1, ProcessType.SECURITY_ANALYSIS),
            new MetadataInfos(processConfigId2, ProcessType.SECURITY_ANALYSIS)
        ));
    }

    @Test
    void updateSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = securityAnalysisConfigMapper.toEntity(securityAnalysisConfig);

        SecurityAnalysisConfig newSecurityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                UUID.randomUUID()
        );

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(securityAnalysisConfigEntity));

        boolean done = processConfigService.updateProcessConfig(processConfigId, newSecurityAnalysisConfig);
        assertThat(done).isTrue();

        verify(processConfigRepository).findById(processConfigId);

        Optional<PersistedProcessConfig> processConfigUpdated = processConfigService.getProcessConfig(processConfigId);
        assertThat(processConfigUpdated).isPresent();
        assertThat(processConfigUpdated.get().processConfig()).usingRecursiveComparison().isEqualTo(newSecurityAnalysisConfig);
    }

    @Test
    void updateSecurityAnalysisConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        SecurityAnalysisConfig newSecurityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                UUID.randomUUID()
        );
        boolean done = processConfigService.updateProcessConfig(processConfigId, newSecurityAnalysisConfig);
        assertThat(done).isFalse();

        verify(processConfigRepository).findById(processConfigId);
    }

    @Test
    void duplicateSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        UUID expectedNewProcessConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId))
            .thenReturn(Optional.of(mock(SecurityAnalysisConfigEntity.class)));
        when(processConfigRepository.save(any(SecurityAnalysisConfigEntity.class)))
            .thenAnswer(invocation -> {
                SecurityAnalysisConfigEntity entity = invocation.getArgument(0);
                entity.setId(expectedNewProcessConfigId);
                return entity;
            });

        Optional<UUID> newProcessConfigId = processConfigService.duplicateProcessConfig(processConfigId);

        assertThat(newProcessConfigId).isPresent();
        assertThat(newProcessConfigId.get()).isEqualTo(expectedNewProcessConfigId);

        verify(processConfigRepository).findById(processConfigId);
        ArgumentCaptor<SecurityAnalysisConfigEntity> captor = ArgumentCaptor.forClass(SecurityAnalysisConfigEntity.class);
        verify(processConfigRepository).save(captor.capture());
    }

    @Test
    void duplicateProcessConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        Optional<UUID> newProcessConfigId = processConfigService.duplicateProcessConfig(processConfigId);
        assertThat(newProcessConfigId).isEmpty();
        verify(processConfigRepository).findById(processConfigId);
    }

    @Test
    void deleteProcesConfig() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.existsById(processConfigId)).thenReturn(Boolean.TRUE);
        doNothing().when(processConfigRepository).deleteById(processConfigId);

        boolean done = processConfigService.deleteProcessConfig(processConfigId);
        assertThat(done).isTrue();

        verify(processConfigRepository).existsById(processConfigId);
        verify(processConfigRepository).deleteById(processConfigId);
    }

    @Test
    void deleteProcessConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.existsById(processConfigId)).thenReturn(Boolean.FALSE);

        boolean done = processConfigService.deleteProcessConfig(processConfigId);
        assertThat(done).isFalse();

        verify(processConfigRepository).existsById(processConfigId);
        verify(processConfigRepository, never()).deleteById(processConfigId);
    }

    @Test
    void getSecurityAnalysisConfigs() {
        SecurityAnalysisConfig securityAnalysisConfig1 = new SecurityAnalysisConfig(UUID.randomUUID(), List.of(UUID.randomUUID(), UUID.randomUUID()), UUID.randomUUID());
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity1 = securityAnalysisConfigMapper.toEntity(securityAnalysisConfig1);
        SecurityAnalysisConfig securityAnalysisConfig2 = new SecurityAnalysisConfig(UUID.randomUUID(), List.of(UUID.randomUUID()), UUID.randomUUID());
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity2 = securityAnalysisConfigMapper.toEntity(securityAnalysisConfig2);

        when(processConfigRepository.findAllByProcessType(ProcessType.SECURITY_ANALYSIS))
                .thenReturn(List.of(securityAnalysisConfigEntity1, securityAnalysisConfigEntity2));

        List<PersistedProcessConfig> processConfigs = processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);

        verify(processConfigRepository).findAllByProcessType(ProcessType.SECURITY_ANALYSIS);
        assertThat(processConfigs).hasSize(2);
        assertThat(processConfigs.get(0).processConfig().processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        assertThat(processConfigs.get(1).processConfig().processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);

        SecurityAnalysisConfig resSecurityAnalysisConfig1 = (SecurityAnalysisConfig) processConfigs.get(0).processConfig();
        assertThat(resSecurityAnalysisConfig1.securityAnalysisParametersUuid()).isEqualTo(securityAnalysisConfig1.securityAnalysisParametersUuid());
        assertThat(resSecurityAnalysisConfig1.modificationUuids()).isEqualTo(securityAnalysisConfig1.modificationUuids());

        SecurityAnalysisConfig resSecurityAnalysisConfig2 = (SecurityAnalysisConfig) processConfigs.get(1).processConfig();
        assertThat(resSecurityAnalysisConfig2.securityAnalysisParametersUuid()).isEqualTo(securityAnalysisConfig2.securityAnalysisParametersUuid());
        assertThat(resSecurityAnalysisConfig2.modificationUuids()).isEqualTo(securityAnalysisConfig2.modificationUuids());
    }

    @Test
    void getSecurityAnalysisConfigsNotFound() {
        when(processConfigRepository.findAllByProcessType(ProcessType.SECURITY_ANALYSIS)).thenReturn(List.of());

        List<PersistedProcessConfig> processConfigs = processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);

        verify(processConfigRepository).findAllByProcessType(ProcessType.SECURITY_ANALYSIS);
        assertThat(processConfigs).isEmpty();
    }

    @Test
    void createLoadFlowConfig() {
        UUID expectedProcessConfigId = UUID.randomUUID();
        when(processConfigRepository.save(any(LoadFlowConfigEntity.class)))
            .thenAnswer(invocation -> {
                LoadFlowConfigEntity entity = invocation.getArgument(0);
                entity.setId(expectedProcessConfigId);
                return entity;
            });

        UUID result = processConfigService.createProcessConfig(loadFlowConfig);
        assertThat(result).isEqualTo(expectedProcessConfigId);

        ArgumentCaptor<LoadFlowConfigEntity> captor = ArgumentCaptor.forClass(LoadFlowConfigEntity.class);
        verify(processConfigRepository).save(captor.capture());

        LoadFlowConfigEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getId()).isEqualTo(expectedProcessConfigId);
        assertThat(savedEntity.getProcessType()).isEqualTo(ProcessType.LOADFLOW);
        assertThat(savedEntity.getLoadflowParametersUuid()).isEqualTo(loadFlowConfig.loadflowParametersUuid());
        assertThat(savedEntity.getModificationUuids()).isEqualTo(loadFlowConfig.modificationUuids());
    }

    @Test
    void getLoadFlowConfig() {
        UUID processConfigId = UUID.randomUUID();
        LoadFlowConfigEntity loadFlowConfigEntity = loadFlowConfigMapper.toEntity(loadFlowConfig);

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(loadFlowConfigEntity));

        Optional<PersistedProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isPresent();
        assertThat(processConfig.get().processConfig()).usingRecursiveComparison().isEqualTo(loadFlowConfig);
    }

    @Test
    void updateLoadFlowConfig() {
        UUID processConfigId = UUID.randomUUID();
        LoadFlowConfigEntity loadFlowConfigEntity = loadFlowConfigMapper.toEntity(loadFlowConfig);

        LoadFlowConfig newLoadFlowConfig = new LoadFlowConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID())
        );

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(loadFlowConfigEntity));

        boolean done = processConfigService.updateProcessConfig(processConfigId, newLoadFlowConfig);
        assertThat(done).isTrue();

        verify(processConfigRepository).findById(processConfigId);

        Optional<PersistedProcessConfig> processConfigUpdated = processConfigService.getProcessConfig(processConfigId);
        assertThat(processConfigUpdated).isPresent();
        assertThat(processConfigUpdated.get().processConfig()).usingRecursiveComparison().isEqualTo(newLoadFlowConfig);
    }

    @Test
    void updateLoadFlowConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        LoadFlowConfig newLoadFlowConfig = new LoadFlowConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID())
        );

        boolean done = processConfigService.updateProcessConfig(processConfigId, newLoadFlowConfig);
        assertThat(done).isFalse();

        verify(processConfigRepository).findById(processConfigId);
    }

    @Test
    void duplicateLoadFlowConfig() {
        UUID processConfigId = UUID.randomUUID();
        UUID expectedNewProcessConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId))
            .thenReturn(Optional.of(mock(LoadFlowConfigEntity.class)));
        when(processConfigRepository.save(any(LoadFlowConfigEntity.class)))
            .thenAnswer(invocation -> {
                LoadFlowConfigEntity entity = invocation.getArgument(0);
                entity.setId(expectedNewProcessConfigId);
                return entity;
            });

        Optional<UUID> newProcessConfigId = processConfigService.duplicateProcessConfig(processConfigId);

        assertThat(newProcessConfigId).isPresent();
        assertThat(newProcessConfigId.get()).isEqualTo(expectedNewProcessConfigId);

        verify(processConfigRepository).findById(processConfigId);
        ArgumentCaptor<LoadFlowConfigEntity> captor = ArgumentCaptor.forClass(LoadFlowConfigEntity.class);
        verify(processConfigRepository).save(captor.capture());
    }

    @Test
    void getLoadFlowConfigs() {
        LoadFlowConfig loadFlowConfig1 = new LoadFlowConfig(UUID.randomUUID(), List.of(UUID.randomUUID(), UUID.randomUUID()));
        LoadFlowConfigEntity loadFlowConfigEntity1 = loadFlowConfigMapper.toEntity(loadFlowConfig1);

        LoadFlowConfig loadFlowConfig2 = new LoadFlowConfig(UUID.randomUUID(), List.of(UUID.randomUUID()));
        LoadFlowConfigEntity loadFlowConfigEntity2 = loadFlowConfigMapper.toEntity(loadFlowConfig2);

        when(processConfigRepository.findAllByProcessType(ProcessType.LOADFLOW))
            .thenReturn(List.of(loadFlowConfigEntity1, loadFlowConfigEntity2));

        List<PersistedProcessConfig> processConfigs = processConfigService.getProcessConfigs(ProcessType.LOADFLOW);

        verify(processConfigRepository).findAllByProcessType(ProcessType.LOADFLOW);
        assertThat(processConfigs).hasSize(2);
        assertThat(processConfigs.get(0).processConfig().processType()).isEqualTo(ProcessType.LOADFLOW);
        assertThat(processConfigs.get(1).processConfig().processType()).isEqualTo(ProcessType.LOADFLOW);

        LoadFlowConfig resLoadFlowConfig1 = (LoadFlowConfig) processConfigs.get(0).processConfig();
        assertThat(resLoadFlowConfig1.loadflowParametersUuid()).isEqualTo(loadFlowConfig1.loadflowParametersUuid());
        assertThat(resLoadFlowConfig1.modificationUuids()).isEqualTo(loadFlowConfig1.modificationUuids());

        LoadFlowConfig resLoadFlowConfig2 = (LoadFlowConfig) processConfigs.get(1).processConfig();
        assertThat(resLoadFlowConfig2.loadflowParametersUuid()).isEqualTo(loadFlowConfig2.loadflowParametersUuid());
        assertThat(resLoadFlowConfig2.modificationUuids()).isEqualTo(loadFlowConfig2.modificationUuids());
    }

    @Test
    void getLoadFlowConfigsNotFound() {
        when(processConfigRepository.findAllByProcessType(ProcessType.LOADFLOW)).thenReturn(List.of());

        List<PersistedProcessConfig> processConfigs = processConfigService.getProcessConfigs(ProcessType.LOADFLOW);

        verify(processConfigRepository).findAllByProcessType(ProcessType.LOADFLOW);
        assertThat(processConfigs).isEmpty();
    }

    @Test
    void getLoadFlowConfigsMetadata() {
        UUID processConfigId1 = UUID.randomUUID();
        UUID processConfigId2 = UUID.randomUUID();

        LoadFlowConfigEntity entity1 = loadFlowConfigMapper.toEntity(loadFlowConfig);
        entity1.setId(processConfigId1);
        LoadFlowConfigEntity entity2 = loadFlowConfigMapper.toEntity(loadFlowConfig);
        entity2.setId(processConfigId2);

        when(processConfigRepository.findAllById(List.of(processConfigId1, processConfigId2)))
            .thenReturn(List.of(entity1, entity2));

        List<MetadataInfos> metadataInfos = processConfigService.getProcessConfigsMetadata(List.of(processConfigId1, processConfigId2));

        verify(processConfigRepository).findAllById(List.of(processConfigId1, processConfigId2));
        assertThat(metadataInfos).isEqualTo(List.of(
            new MetadataInfos(processConfigId1, ProcessType.LOADFLOW),
            new MetadataInfos(processConfigId2, ProcessType.LOADFLOW)
        ));
    }

    @Test
    void compareProcessConfigsShouldReturnIdenticalWhenConfigsAreEqual() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid);
        entity1.setModificationUuids(modificationUuids);
        entity1.setLoadflowParametersUuid(loadflowParametersUuid);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid);
        entity2.setModificationUuids(modificationUuids);
        entity2.setLoadflowParametersUuid(loadflowParametersUuid);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.processConfigUuid1()).isEqualTo(uuid1);
        assertThat(comparison.processConfigUuid2()).isEqualTo(uuid2);
        assertThat(comparison.identical()).isTrue();
        assertThat(comparison.differences()).hasSize(3);
        assertThat(comparison.differences()).allMatch(ProcessConfigFieldComparison::identical);
    }

    @Test
    void compareProcessConfigsShouldReturnDifferencesWhenModificationsAreDifferent() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> modificationUuids2 = List.of(UUID.randomUUID(), UUID.randomUUID());

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid);
        entity1.setModificationUuids(modificationUuids1);
        entity1.setLoadflowParametersUuid(loadflowParametersUuid);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid);
        entity2.setModificationUuids(modificationUuids2);
        entity2.setLoadflowParametersUuid(loadflowParametersUuid);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.differences()).hasSize(3);

        ProcessConfigFieldComparison modificationsComparison = comparison.differences().stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(modificationsComparison.identical()).isFalse();
        assertThat(modificationsComparison.value1()).isEqualTo(modificationUuids1);
        assertThat(modificationsComparison.value2()).isEqualTo(modificationUuids2);
    }

    @Test
    void compareProcessConfigsShouldReturnDifferencesWhenParametersAreDifferent() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid1 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid2 = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID());

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid1);
        entity1.setModificationUuids(modificationUuids);
        entity1.setLoadflowParametersUuid(loadflowParametersUuid);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid2);
        entity2.setModificationUuids(modificationUuids);
        entity2.setLoadflowParametersUuid(loadflowParametersUuid);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.identical()).isFalse();

        ProcessConfigFieldComparison parametersComparison = comparison.differences().stream()
            .filter(d -> "securityAnalysisParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(parametersComparison.identical()).isFalse();
        assertThat(parametersComparison.value1()).isEqualTo(securityAnalysisParametersUuid1);
        assertThat(parametersComparison.value2()).isEqualTo(securityAnalysisParametersUuid2);
    }

    @Test
    void compareProcessConfigsShouldDetectOrderDifferenceInModifications() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(mod1, mod2);
        List<UUID> modificationUuids2 = List.of(mod2, mod1); // Different order

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid);
        entity1.setModificationUuids(modificationUuids1);
        entity1.setLoadflowParametersUuid(loadflowParametersUuid);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setSecurityAnalysisParametersUuid(securityAnalysisParametersUuid);
        entity2.setModificationUuids(modificationUuids2);
        entity2.setLoadflowParametersUuid(loadflowParametersUuid);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.identical()).isFalse();

        ProcessConfigFieldComparison modificationsComparison = comparison.differences().stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(modificationsComparison.identical()).isFalse();
    }

    @Test
    void compareLoadFlowConfigsShouldReturnIdenticalWhenConfigsAreEqual() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());

        LoadFlowConfigEntity entity1 = new LoadFlowConfigEntity();
        entity1.setId(uuid1);
        entity1.setLoadflowParametersUuid(loadflowParametersUuid);
        entity1.setModificationUuids(modificationUuids);

        LoadFlowConfigEntity entity2 = new LoadFlowConfigEntity();
        entity2.setId(uuid2);
        entity2.setLoadflowParametersUuid(loadflowParametersUuid);
        entity2.setModificationUuids(modificationUuids);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.processConfigUuid1()).isEqualTo(uuid1);
        assertThat(comparison.processConfigUuid2()).isEqualTo(uuid2);
        assertThat(comparison.identical()).isTrue();
        assertThat(comparison.differences()).hasSize(2);
        assertThat(comparison.differences()).allMatch(ProcessConfigFieldComparison::identical);
    }

    @Test
    void compareLoadFlowConfigsShouldReturnDifferencesWhenModificationsAreDifferent() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> modificationUuids2 = List.of(UUID.randomUUID(), UUID.randomUUID());

        LoadFlowConfigEntity entity1 = new LoadFlowConfigEntity();
        entity1.setId(uuid1);
        entity1.setLoadflowParametersUuid(loadflowParametersUuid);
        entity1.setModificationUuids(modificationUuids1);

        LoadFlowConfigEntity entity2 = new LoadFlowConfigEntity();
        entity2.setId(uuid2);
        entity2.setLoadflowParametersUuid(loadflowParametersUuid);
        entity2.setModificationUuids(modificationUuids2);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.differences()).hasSize(2);

        ProcessConfigFieldComparison modificationsComparison = comparison.differences().stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(modificationsComparison.identical()).isFalse();
        assertThat(modificationsComparison.value1()).isEqualTo(modificationUuids1);
        assertThat(modificationsComparison.value2()).isEqualTo(modificationUuids2);
    }

    @Test
    void compareLoadFlowConfigsShouldReturnDifferencesWhenParametersAreDifferent() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID loadflowParametersUuid1 = UUID.randomUUID();
        UUID loadflowParametersUuid2 = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID());

        LoadFlowConfigEntity entity1 = new LoadFlowConfigEntity();
        entity1.setId(uuid1);
        entity1.setLoadflowParametersUuid(loadflowParametersUuid1);
        entity1.setModificationUuids(modificationUuids);

        LoadFlowConfigEntity entity2 = new LoadFlowConfigEntity();
        entity2.setId(uuid2);
        entity2.setLoadflowParametersUuid(loadflowParametersUuid2);
        entity2.setModificationUuids(modificationUuids);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.identical()).isFalse();

        ProcessConfigFieldComparison parametersComparison = comparison.differences().stream()
            .filter(d -> "loadflowParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(parametersComparison.identical()).isFalse();
        assertThat(parametersComparison.value1()).isEqualTo(loadflowParametersUuid1);
        assertThat(parametersComparison.value2()).isEqualTo(loadflowParametersUuid2);
    }

    @Test
    void compareProcessConfigsShouldReturnEmptyWhenOneConfigNotFound() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.empty());
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(new SecurityAnalysisConfigEntity()));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isEmpty();
    }
}
