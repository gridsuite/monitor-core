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
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID()),
                null, null, null, null
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

        UUID result = processConfigService.createProcessConfig(securityAnalysisConfig, "user1");
        assertThat(result).isEqualTo(expectedProcessConfigId);

        ArgumentCaptor<SecurityAnalysisConfigEntity> captor = ArgumentCaptor.forClass(SecurityAnalysisConfigEntity.class);
        verify(processConfigRepository).save(captor.capture());

        SecurityAnalysisConfigEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getId()).isEqualTo(expectedProcessConfigId);
        assertThat(savedEntity.getType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        assertThat(savedEntity.getParametersUuid()).isEqualTo(securityAnalysisConfig.getParametersUuid());
        assertThat(savedEntity.getContingencies()).isEqualTo(securityAnalysisConfig.getContingencies());
        assertThat(savedEntity.getModificationUuids()).isEqualTo(securityAnalysisConfig.getModificationUuids());
        assertThat(savedEntity.getOwner()).isEqualTo("user1");
        assertThat(savedEntity.getCreationDate()).isNotNull();
        assertThat(savedEntity.getLastModificationDate()).isNotNull();
        assertThat(savedEntity.getLastModifiedBy()).isEqualTo("user1");
    }

    @Test
    void getSecurityAnalysisConfig() {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig, "user1");

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(securityAnalysisConfigEntity));

        Optional<ProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isPresent();
        assertThat(processConfig.get().processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        SecurityAnalysisConfig resSecurityAnalysisConfig = (SecurityAnalysisConfig) processConfig.get();
        assertThat(resSecurityAnalysisConfig.getParametersUuid()).isEqualTo(securityAnalysisConfig.getParametersUuid());
        assertThat(resSecurityAnalysisConfig.getContingencies()).isEqualTo(securityAnalysisConfig.getContingencies());
        assertThat(resSecurityAnalysisConfig.getModificationUuids()).isEqualTo(securityAnalysisConfig.getModificationUuids());
        assertThat(resSecurityAnalysisConfig.getOwner()).isEqualTo("user1");
        assertThat(resSecurityAnalysisConfig.getCreationDate()).isNotNull();
        assertThat(resSecurityAnalysisConfig.getLastModificationDate()).isNotNull();
        assertThat(resSecurityAnalysisConfig.getLastModifiedBy()).isEqualTo("user1");
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
        SecurityAnalysisConfigEntity securityAnalysisConfigEntity = SecurityAnalysisConfigMapper.toEntity(securityAnalysisConfig, "user1");

        SecurityAnalysisConfig newSecurityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("contingency3", "contingency4", "contingency5"),
                List.of(UUID.randomUUID()),
                null, null, null, null
        );

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(securityAnalysisConfigEntity));

        boolean done = processConfigService.updateProcessConfig(processConfigId, newSecurityAnalysisConfig, "user2");
        assertThat(done).isTrue();

        verify(processConfigRepository).findById(processConfigId);

        Optional<ProcessConfig> processConfigUpdated = processConfigService.getProcessConfig(processConfigId);
        assertThat(processConfigUpdated).isPresent();
        SecurityAnalysisConfig updatedSecurityAnalysisConfig = (SecurityAnalysisConfig) processConfigUpdated.get();
        assertThat(updatedSecurityAnalysisConfig.getParametersUuid()).isEqualTo(newSecurityAnalysisConfig.getParametersUuid());
        assertThat(updatedSecurityAnalysisConfig.getContingencies()).isEqualTo(newSecurityAnalysisConfig.getContingencies());
        assertThat(updatedSecurityAnalysisConfig.getModificationUuids()).isEqualTo(newSecurityAnalysisConfig.getModificationUuids());
        assertThat(updatedSecurityAnalysisConfig.getOwner()).isEqualTo("user1");
        assertThat(updatedSecurityAnalysisConfig.getCreationDate()).isNotNull();
        assertThat(updatedSecurityAnalysisConfig.getLastModificationDate()).isNotNull();
        assertThat(updatedSecurityAnalysisConfig.getLastModifiedBy()).isEqualTo("user2");
    }

    @Test
    void updateSecurityAnalysisConfigNotFound() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.empty());

        SecurityAnalysisConfig newSecurityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("contingency1"),
                List.of(UUID.randomUUID()),
                null, null, null, null
        );
        boolean done = processConfigService.updateProcessConfig(processConfigId, newSecurityAnalysisConfig, "user3");
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
}
