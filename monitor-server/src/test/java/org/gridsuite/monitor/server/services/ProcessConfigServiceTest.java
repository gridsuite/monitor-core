/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.PersistedProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.dto.ProcessConfigComparison;
import org.gridsuite.monitor.server.dto.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mapper.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setUp() {
        securityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("contingency1", "contingency2"),
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
        assertThat(savedEntity.getParametersUuid()).isEqualTo(securityAnalysisConfig.parametersUuid());
        assertThat(savedEntity.getContingencies()).isEqualTo(securityAnalysisConfig.contingencies());
        assertThat(savedEntity.getModificationUuids()).isEqualTo(securityAnalysisConfig.modificationUuids());
    }

    @Test
    void getSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig);

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(securityAnalysisConfigEntity));

        Optional<PersistedProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isPresent();
        assertThat(processConfig.get().processConfig()).usingRecursiveComparison().isEqualTo(securityAnalysisConfig);
    }

    @Test
    void getSecurityAnalysisConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        Optional<PersistedProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isEmpty();
    }

    @Test
    void updateSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig);

        SecurityAnalysisConfig newSecurityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("contingency3", "contingency4", "contingency5"),
                List.of(UUID.randomUUID())
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
                List.of("contingency1"),
                List.of(UUID.randomUUID())
        );
        boolean done = processConfigService.updateProcessConfig(processConfigId, newSecurityAnalysisConfig);
        assertThat(done).isFalse();

        verify(processConfigRepository).findById(processConfigId);
    }

    @Test
    void deleteSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.existsById(processConfigId)).thenReturn(Boolean.TRUE);
        doNothing().when(processConfigRepository).deleteById(processConfigId);

        boolean done = processConfigService.deleteProcessConfig(processConfigId);
        assertThat(done).isTrue();

        verify(processConfigRepository).existsById(processConfigId);
        verify(processConfigRepository).deleteById(processConfigId);
    }

    @Test
    void deleteSecurityAnalysisConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.existsById(processConfigId)).thenReturn(Boolean.FALSE);

        boolean done = processConfigService.deleteProcessConfig(processConfigId);
        assertThat(done).isFalse();

        verify(processConfigRepository).existsById(processConfigId);
        verify(processConfigRepository, never()).deleteById(processConfigId);
    }

    @Test
    void getSecurityAnalysisConfigs() {
        SecurityAnalysisConfig securityAnalysisConfig1 = new SecurityAnalysisConfig(UUID.randomUUID(), List.of("contingency1", "contingency2"), List.of(UUID.randomUUID()));
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity1 = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig1);
        SecurityAnalysisConfig securityAnalysisConfig2 = new SecurityAnalysisConfig(UUID.randomUUID(), List.of("contingency3", "contingency4"), List.of(UUID.randomUUID()));
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity2 = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig2);

        when(processConfigRepository.findAllByProcessType(ProcessType.SECURITY_ANALYSIS))
            .thenReturn(List.of(securityAnalysisConfigEntity1, securityAnalysisConfigEntity2));

        List<PersistedProcessConfig> processConfigs = processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);

        verify(processConfigRepository).findAllByProcessType(ProcessType.SECURITY_ANALYSIS);
        assertThat(processConfigs).hasSize(2);
        assertThat(processConfigs.get(0).processConfig().processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        assertThat(processConfigs.get(1).processConfig().processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);

        SecurityAnalysisConfig resSecurityAnalysisConfig1 = (SecurityAnalysisConfig) processConfigs.get(0).processConfig();
        assertThat(resSecurityAnalysisConfig1.parametersUuid()).isEqualTo(securityAnalysisConfig1.parametersUuid());
        assertThat(resSecurityAnalysisConfig1.contingencies()).isEqualTo(securityAnalysisConfig1.contingencies());
        assertThat(resSecurityAnalysisConfig1.modificationUuids()).isEqualTo(securityAnalysisConfig1.modificationUuids());

        SecurityAnalysisConfig resSecurityAnalysisConfig2 = (SecurityAnalysisConfig) processConfigs.get(1).processConfig();
        assertThat(resSecurityAnalysisConfig2.parametersUuid()).isEqualTo(securityAnalysisConfig2.parametersUuid());
        assertThat(resSecurityAnalysisConfig2.contingencies()).isEqualTo(securityAnalysisConfig2.contingencies());
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
    void compareProcessConfigsShouldReturnIdenticalWhenConfigsAreEqual() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID parametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<String> contingencies = List.of("contingency1", "contingency2");

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setParametersUuid(parametersUuid);
        entity1.setModificationUuids(modificationUuids);
        entity1.setContingencies(contingencies);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setParametersUuid(parametersUuid);
        entity2.setModificationUuids(modificationUuids);
        entity2.setContingencies(contingencies);

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
        UUID parametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> modificationUuids2 = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<String> contingencies = List.of("contingency1", "contingency2");

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setParametersUuid(parametersUuid);
        entity1.setModificationUuids(modificationUuids1);
        entity1.setContingencies(contingencies);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setParametersUuid(parametersUuid);
        entity2.setModificationUuids(modificationUuids2);
        entity2.setContingencies(contingencies);

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
        UUID parametersUuid1 = UUID.randomUUID();
        UUID parametersUuid2 = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID());
        List<String> contingencies = List.of("contingency1");

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setParametersUuid(parametersUuid1);
        entity1.setModificationUuids(modificationUuids);
        entity1.setContingencies(contingencies);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setParametersUuid(parametersUuid2);
        entity2.setModificationUuids(modificationUuids);
        entity2.setContingencies(contingencies);

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
        assertThat(parametersComparison.value1()).isEqualTo(parametersUuid1);
        assertThat(parametersComparison.value2()).isEqualTo(parametersUuid2);
    }

    @Test
    void compareProcessConfigsShouldReturnDifferencesWhenContingenciesAreDifferent() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID parametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID());
        List<String> contingencies1 = List.of("contingency1", "contingency2");
        List<String> contingencies2 = List.of("contingency1", "contingency3");

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setParametersUuid(parametersUuid);
        entity1.setModificationUuids(modificationUuids);
        entity1.setContingencies(contingencies1);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setParametersUuid(parametersUuid);
        entity2.setModificationUuids(modificationUuids);
        entity2.setContingencies(contingencies2);

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.of(entity1));
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(entity2));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isPresent();
        ProcessConfigComparison comparison = result.get();
        assertThat(comparison.identical()).isFalse();

        ProcessConfigFieldComparison contingenciesComparison = comparison.differences().stream()
            .filter(d -> "contingencies".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(contingenciesComparison.identical()).isFalse();
        assertThat(contingenciesComparison.value1()).isEqualTo(contingencies1);
        assertThat(contingenciesComparison.value2()).isEqualTo(contingencies2);
    }

    @Test
    void compareProcessConfigsShouldDetectOrderDifferenceInModifications() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID parametersUuid = UUID.randomUUID();
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(mod1, mod2);
        List<UUID> modificationUuids2 = List.of(mod2, mod1); // Different order
        List<String> contingencies = List.of("contingency1");

        SecurityAnalysisConfigEntity entity1 = new SecurityAnalysisConfigEntity();
        entity1.setId(uuid1);
        entity1.setParametersUuid(parametersUuid);
        entity1.setModificationUuids(modificationUuids1);
        entity1.setContingencies(contingencies);

        SecurityAnalysisConfigEntity entity2 = new SecurityAnalysisConfigEntity();
        entity2.setId(uuid2);
        entity2.setParametersUuid(parametersUuid);
        entity2.setModificationUuids(modificationUuids2);
        entity2.setContingencies(contingencies);

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
    void compareProcessConfigsShouldReturnEmptyWhenOneConfigNotFound() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        when(processConfigRepository.findById(uuid1)).thenReturn(Optional.empty());
        when(processConfigRepository.findById(uuid2)).thenReturn(Optional.of(new SecurityAnalysisConfigEntity()));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(uuid1, uuid2);

        assertThat(result).isEmpty();
    }
}
