/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
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
        assertThat(savedEntity.getType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        assertThat(savedEntity.getParametersUuid()).isEqualTo(securityAnalysisConfig.parametersUuid());
        assertThat(savedEntity.getContingencies()).isEqualTo(securityAnalysisConfig.contingencies());
        assertThat(savedEntity.getModificationUuids()).isEqualTo(securityAnalysisConfig.modificationUuids());
    }

    @Test
    void getSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig);

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(securityAnalysisConfigEntity));

        Optional<ProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isPresent();
        assertThat(processConfig.get()).usingRecursiveComparison().isEqualTo(securityAnalysisConfig);
    }

    @Test
    void getSecurityAnalysisConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        Optional<ProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isEmpty();
    }

    @Test
    void updateSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig);

        SecurityAnalysisConfig newSecurityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of("contingency3", "contingency4", "contingency5"),
                List.of(UUID.randomUUID())
        );

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(securityAnalysisConfigEntity));

        boolean done = processConfigService.updateProcessConfig(processConfigId, newSecurityAnalysisConfig);
        assertThat(done).isTrue();

        verify(processConfigRepository).findById(processConfigId);

        Optional<ProcessConfig> processConfigUpdated = processConfigService.getProcessConfig(processConfigId);
        assertThat(processConfigUpdated).isPresent();
        assertThat(processConfigUpdated.get()).usingRecursiveComparison().isEqualTo(newSecurityAnalysisConfig);
    }

    @Test
    void updateSecurityAnalysisConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        SecurityAnalysisConfig newSecurityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
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
        SecurityAnalysisConfig securityAnalysisConfig1 = new SecurityAnalysisConfig(UUID.randomUUID(), UUID.randomUUID(), List.of("contingency1", "contingency2"), List.of(UUID.randomUUID()), null, null, null, null);
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity1 = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig1, "user1");
        SecurityAnalysisConfig securityAnalysisConfig2 = new SecurityAnalysisConfig(UUID.randomUUID(), UUID.randomUUID(), List.of("contingency3", "contingency4"), List.of(UUID.randomUUID()), null, null, null, null);
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity2 = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig2, "user2");

        when(processConfigRepository.findAllByProcessTypeOrderByLastModificationDateDesc(SecurityAnalysisConfigEntity.class))
            .thenReturn(List.of(securityAnalysisConfigEntity1, securityAnalysisConfigEntity2));

        Optional<List<ProcessConfig>> processConfigs = processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);

        verify(processConfigRepository).findAllByProcessTypeOrderByLastModificationDateDesc(SecurityAnalysisConfigEntity.class);
        assertThat(processConfigs).isPresent();
        assertThat(processConfigs.get()).hasSize(2);
        assertThat(processConfigs.get().get(0).processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        assertThat(processConfigs.get().get(1).processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);

        SecurityAnalysisConfig resSecurityAnalysisConfig1 = (SecurityAnalysisConfig) processConfigs.get().get(0);
        assertThat(resSecurityAnalysisConfig1.getParametersUuid()).isEqualTo(securityAnalysisConfig1.getParametersUuid());
        assertThat(resSecurityAnalysisConfig1.getContingencies()).isEqualTo(securityAnalysisConfig1.getContingencies());
        assertThat(resSecurityAnalysisConfig1.getModificationUuids()).isEqualTo(securityAnalysisConfig1.getModificationUuids());
        assertThat(resSecurityAnalysisConfig1.getOwner()).isEqualTo("user1");
        assertThat(resSecurityAnalysisConfig1.getCreationDate()).isNotNull();
        assertThat(resSecurityAnalysisConfig1.getLastModificationDate()).isNotNull();
        assertThat(resSecurityAnalysisConfig1.getLastModifiedBy()).isEqualTo("user1");

        SecurityAnalysisConfig resSecurityAnalysisConfig2 = (SecurityAnalysisConfig) processConfigs.get().get(1);
        assertThat(resSecurityAnalysisConfig2.getParametersUuid()).isEqualTo(securityAnalysisConfig2.getParametersUuid());
        assertThat(resSecurityAnalysisConfig2.getContingencies()).isEqualTo(securityAnalysisConfig2.getContingencies());
        assertThat(resSecurityAnalysisConfig2.getModificationUuids()).isEqualTo(securityAnalysisConfig2.getModificationUuids());
        assertThat(resSecurityAnalysisConfig2.getOwner()).isEqualTo("user2");
        assertThat(resSecurityAnalysisConfig2.getCreationDate()).isNotNull();
        assertThat(resSecurityAnalysisConfig2.getLastModificationDate()).isNotNull();
        assertThat(resSecurityAnalysisConfig2.getLastModifiedBy()).isEqualTo("user2");
    }

    @Test
    void getSecurityAnalysisConfigsNotFound() {
        when(processConfigRepository.findAllByProcessTypeOrderByLastModificationDateDesc(SecurityAnalysisConfigEntity.class)).thenReturn(List.of());

        Optional<List<ProcessConfig>> processConfigs = processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);

        verify(processConfigRepository).findAllByProcessTypeOrderByLastModificationDateDesc(SecurityAnalysisConfigEntity.class);
        assertThat(processConfigs).isEmpty();
    }
}
