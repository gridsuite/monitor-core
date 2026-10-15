/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.MetadataInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROVIDER;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessConfigServiceTest {
    @Mock
    private ProcessConfigValidationService processConfigValidationService;
    @Mock
    private ProcessConfigTxService processConfigTxService;
    @Mock
    private ProcessConfig processConfig;

    private ProcessConfigService processConfigService;

    @BeforeEach
    void setUp() {
        processConfigService = new ProcessConfigService(processConfigValidationService, processConfigTxService);
    }

    @Test
    void createProcessConfig() {
        UUID uuid = UUID.randomUUID();
        when(processConfigTxService.createProcessConfig(processConfig)).thenReturn(uuid);

        assertThat(processConfigService.createProcessConfig(processConfig)).isEqualTo(uuid);

        InOrder inOrder = inOrder(processConfigValidationService, processConfigTxService);
        inOrder.verify(processConfigValidationService).validate(processConfig);
        inOrder.verify(processConfigTxService).createProcessConfig(processConfig);
    }

    @Test
    void createProcessConfigWithInvalidConfig() {
        MonitorServerException exception = new MonitorServerException(UNSUPPORTED_PROVIDER, "Invalid provider");
        doThrow(exception).when(processConfigValidationService).validate(processConfig);

        assertThatThrownBy(() -> processConfigService.createProcessConfig(processConfig)).isSameAs(exception);

        verifyNoInteractions(processConfigTxService);
    }

    @Test
    void updateProcessConfig() {
        UUID uuid = UUID.randomUUID();
        when(processConfigTxService.updateProcessConfig(uuid, processConfig)).thenReturn(Optional.of(uuid));

        assertThat(processConfigService.updateProcessConfig(uuid, processConfig)).contains(uuid);

        InOrder inOrder = inOrder(processConfigValidationService, processConfigTxService);
        inOrder.verify(processConfigValidationService).validate(processConfig);
        inOrder.verify(processConfigTxService).updateProcessConfig(uuid, processConfig);
    }

    @Test
    void updateProcessConfigWithInvalidConfig() {
        UUID uuid = UUID.randomUUID();
        MonitorServerException exception = new MonitorServerException(UNSUPPORTED_PROVIDER, "Invalid provider");
        doThrow(exception).when(processConfigValidationService).validate(processConfig);

        assertThatThrownBy(() -> processConfigService.updateProcessConfig(uuid, processConfig)).isSameAs(exception);

        verifyNoInteractions(processConfigTxService);
    }

    @Test
    void updateProcessConfigNotFound() {
        UUID uuid = UUID.randomUUID();
        when(processConfigTxService.updateProcessConfig(uuid, processConfig)).thenReturn(Optional.empty());

        assertThat(processConfigService.updateProcessConfig(uuid, processConfig)).isEmpty();

        verify(processConfigValidationService).validate(processConfig);
        verify(processConfigTxService).updateProcessConfig(uuid, processConfig);
    }

    @Test
    void getProcessConfig() {
        UUID uuid = UUID.randomUUID();
        Optional<PersistedProcessConfig> expected = Optional.of(new PersistedProcessConfig(uuid, processConfig));
        when(processConfigTxService.getProcessConfig(uuid)).thenReturn(expected);

        assertThat(processConfigService.getProcessConfig(uuid)).isEqualTo(expected);

        verify(processConfigTxService).getProcessConfig(uuid);
        verifyNoInteractions(processConfigValidationService);
    }

    @Test
    void getProcessConfigs() {
        UUID uuid = UUID.randomUUID();
        List<PersistedProcessConfig> expected = List.of(new PersistedProcessConfig(uuid, processConfig));
        when(processConfigTxService.getProcessConfigs(ProcessType.LOADFLOW)).thenReturn(expected);

        assertThat(processConfigService.getProcessConfigs(ProcessType.LOADFLOW)).isEqualTo(expected);

        verify(processConfigTxService).getProcessConfigs(ProcessType.LOADFLOW);
        verifyNoInteractions(processConfigValidationService);
    }

    @Test
    void getProcessConfigsMetadata() {
        UUID uuid = UUID.randomUUID();
        List<UUID> uuids = List.of(uuid);
        List<MetadataInfos> expected = List.of(new MetadataInfos(uuid, ProcessType.LOADFLOW));
        when(processConfigTxService.getProcessConfigsMetadata(uuids)).thenReturn(expected);

        assertThat(processConfigService.getProcessConfigsMetadata(uuids)).isEqualTo(expected);

        verify(processConfigTxService).getProcessConfigsMetadata(uuids);
        verifyNoInteractions(processConfigValidationService);
    }

    @Test
    void duplicateProcessConfig() {
        UUID uuid = UUID.randomUUID();
        Optional<UUID> expected = Optional.of(UUID.randomUUID());
        when(processConfigTxService.duplicateProcessConfig(uuid)).thenReturn(expected);

        assertThat(processConfigService.duplicateProcessConfig(uuid)).isEqualTo(expected);

        verify(processConfigTxService).duplicateProcessConfig(uuid);
        verifyNoInteractions(processConfigValidationService);
    }

    @Test
    void deleteProcessConfig() {
        UUID uuid = UUID.randomUUID();
        Optional<UUID> expected = Optional.of(uuid);
        when(processConfigTxService.deleteProcessConfig(uuid)).thenReturn(expected);

        assertThat(processConfigService.deleteProcessConfig(uuid)).isEqualTo(expected);

        verify(processConfigTxService).deleteProcessConfig(uuid);
        verifyNoInteractions(processConfigValidationService);
    }

    @Test
    void compareProcessConfigs() {
        UUID uuid = UUID.randomUUID();
        UUID otherUuid = UUID.randomUUID();
        Optional<ProcessConfigComparison> expected = Optional.of(new ProcessConfigComparison(uuid, otherUuid, true, List.of()));
        when(processConfigTxService.compareProcessConfigs(uuid, otherUuid)).thenReturn(expected);

        assertThat(processConfigService.compareProcessConfigs(uuid, otherUuid)).isEqualTo(expected);

        verify(processConfigTxService).compareProcessConfigs(uuid, otherUuid);
        verifyNoInteractions(processConfigValidationService);
    }
}
