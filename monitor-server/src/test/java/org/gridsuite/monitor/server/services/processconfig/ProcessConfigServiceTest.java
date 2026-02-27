/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.api.types.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.commons.api.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
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
}
