/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.MetadataInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessConfigServiceTest {

    @Mock
    private ProcessConfigTxService processConfigTxService;

    @InjectMocks
    private ProcessConfigService processConfigService;

    private SecurityAnalysisConfig securityAnalysisConfig;
    private LoadFlowConfig loadFlowConfig;

    @BeforeEach
    void setUp() {
        securityAnalysisConfig = new SecurityAnalysisConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID()),
            UUID.randomUUID()
        );
        loadFlowConfig = new LoadFlowConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID())
        );
    }

    @Test
    void createProcessConfigShouldDelegateToTxService() {
        UUID processConfigUuid = UUID.randomUUID();

        when(processConfigTxService.createProcessConfig(securityAnalysisConfig)).thenReturn(processConfigUuid);

        UUID result = processConfigService.createProcessConfig(securityAnalysisConfig);

        assertThat(result).isEqualTo(processConfigUuid);
        verify(processConfigTxService).createProcessConfig(securityAnalysisConfig);
    }

    @Test
    void getProcessConfigShouldDelegateToTxService() {
        UUID processConfigUuid = UUID.randomUUID();
        PersistedProcessConfig persistedProcessConfig = new PersistedProcessConfig(processConfigUuid, securityAnalysisConfig);

        when(processConfigTxService.getProcessConfig(processConfigUuid)).thenReturn(Optional.of(persistedProcessConfig));

        Optional<PersistedProcessConfig> result = processConfigService.getProcessConfig(processConfigUuid);

        assertThat(result).contains(persistedProcessConfig);
        verify(processConfigTxService).getProcessConfig(processConfigUuid);
    }

    @Test
    void getProcessConfigsMetadataShouldDelegateToTxService() {
        UUID processConfigUuid1 = UUID.randomUUID();
        UUID processConfigUuid2 = UUID.randomUUID();
        List<UUID> processConfigUuids = List.of(processConfigUuid1, processConfigUuid2);
        List<MetadataInfos> metadataInfos = List.of(
            new MetadataInfos(processConfigUuid1, ProcessType.SECURITY_ANALYSIS),
            new MetadataInfos(processConfigUuid2, ProcessType.LOADFLOW)
        );

        when(processConfigTxService.getProcessConfigsMetadata(processConfigUuids)).thenReturn(metadataInfos);

        List<MetadataInfos> result = processConfigService.getProcessConfigsMetadata(processConfigUuids);

        assertThat(result).isEqualTo(metadataInfos);
        verify(processConfigTxService).getProcessConfigsMetadata(processConfigUuids);
    }

    @Test
    void updateProcessConfigShouldDelegateToTxService() {
        UUID processConfigUuid = UUID.randomUUID();

        when(processConfigTxService.updateProcessConfig(processConfigUuid, loadFlowConfig)).thenReturn(Optional.of(processConfigUuid));

        Optional<UUID> result = processConfigService.updateProcessConfig(processConfigUuid, loadFlowConfig);

        assertThat(result).contains(processConfigUuid);
        verify(processConfigTxService).updateProcessConfig(processConfigUuid, loadFlowConfig);
    }

    @Test
    void duplicateProcessConfigShouldDelegateToTxService() {
        UUID sourceProcessConfigUuid = UUID.randomUUID();
        UUID duplicatedProcessConfigUuid = UUID.randomUUID();

        when(processConfigTxService.duplicateProcessConfig(sourceProcessConfigUuid)).thenReturn(Optional.of(duplicatedProcessConfigUuid));

        Optional<UUID> result = processConfigService.duplicateProcessConfig(sourceProcessConfigUuid);

        assertThat(result).contains(duplicatedProcessConfigUuid);
        verify(processConfigTxService).duplicateProcessConfig(sourceProcessConfigUuid);
    }

    @Test
    void deleteProcessConfigShouldDelegateToTxService() {
        UUID processConfigUuid = UUID.randomUUID();

        when(processConfigTxService.deleteProcessConfig(processConfigUuid)).thenReturn(Optional.of(processConfigUuid));

        Optional<UUID> result = processConfigService.deleteProcessConfig(processConfigUuid);

        assertThat(result).contains(processConfigUuid);
        verify(processConfigTxService).deleteProcessConfig(processConfigUuid);
    }

    @Test
    void getProcessConfigsShouldDelegateToTxService() {
        List<PersistedProcessConfig> persistedProcessConfigs = List.of(
            new PersistedProcessConfig(UUID.randomUUID(), securityAnalysisConfig),
            new PersistedProcessConfig(UUID.randomUUID(), securityAnalysisConfig)
        );

        when(processConfigTxService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS)).thenReturn(persistedProcessConfigs);

        List<PersistedProcessConfig> result = processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);

        assertThat(result).isEqualTo(persistedProcessConfigs);
        verify(processConfigTxService).getProcessConfigs(ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void compareProcessConfigsShouldDelegateToTxService() {
        UUID processConfigUuid1 = UUID.randomUUID();
        UUID processConfigUuid2 = UUID.randomUUID();
        ProcessConfigComparison comparison = new ProcessConfigComparison(processConfigUuid1, processConfigUuid2, true, List.of());

        when(processConfigTxService.compareProcessConfigs(processConfigUuid1, processConfigUuid2)).thenReturn(Optional.of(comparison));

        Optional<ProcessConfigComparison> result = processConfigService.compareProcessConfigs(processConfigUuid1, processConfigUuid2);

        assertThat(result).contains(comparison);
        verify(processConfigTxService).compareProcessConfigs(processConfigUuid1, processConfigUuid2);
    }

    @Test
    void createProcessConfigShouldAcceptGenericProcessConfigType() {
        UUID processConfigUuid = UUID.randomUUID();
        ProcessConfig processConfig = securityAnalysisConfig;

        when(processConfigTxService.createProcessConfig(processConfig)).thenReturn(processConfigUuid);

        UUID result = processConfigService.createProcessConfig(processConfig);

        assertThat(result).isEqualTo(processConfigUuid);
        verify(processConfigTxService).createProcessConfig(processConfig);
    }
}
