/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.automaticprocessconfig.AutomaticProcessConfigRequest;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigEntity;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigHistoryEntity;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.repositories.automaticprocessconfig.AutomaticProcessConfigHistoryRepository;
import org.gridsuite.monitor.server.repositories.automaticprocessconfig.AutomaticProcessConfigRepository;
import org.gridsuite.monitor.server.services.processconfig.ProcessConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class AutomaticProcessConfigServiceTest {
    @Mock
    private AutomaticProcessConfigRepository repository;
    @Mock
    private AutomaticProcessConfigHistoryRepository historyRepository;
    @Mock
    private ProcessConfigService processConfigService;

    private AutomaticProcessConfigService service;

    @BeforeEach
    void setUp() {
        service = new AutomaticProcessConfigService(repository, historyRepository, processConfigService);
    }

    @Test
    void updatePersistsConfigAndAuditInformation() {
        UUID configUuid = UUID.randomUUID();
        String user = "admin";
        AutomaticProcessConfigRequest request = new AutomaticProcessConfigRequest(ProcessType.LOADFLOW, configUuid);
        when(processConfigService.getProcessConfig(configUuid)).thenReturn(Optional.of(new PersistedProcessConfig(
            configUuid, new LoadFlowConfig(UUID.randomUUID(), List.of())
        )));
        when(repository.findById(ProcessType.LOADFLOW)).thenReturn(Optional.empty());
        when(repository.save(any(AutomaticProcessConfigEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(AutomaticProcessConfigHistoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(request, user);

        assertThat(result).contains(configUuid);
        verify(repository).save(any(AutomaticProcessConfigEntity.class));
        verify(historyRepository).save(any(AutomaticProcessConfigHistoryEntity.class));
    }

    @Test
    void updateReturnsEmptyWhenProcessConfigDoesNotExist() {
        UUID configUuid = UUID.randomUUID();
        when(processConfigService.getProcessConfig(configUuid)).thenReturn(Optional.empty());

        assertThat(service.update(new AutomaticProcessConfigRequest(ProcessType.LOADFLOW, configUuid), "admin")).isEmpty();

        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void updateRejectsProcessConfigWithDifferentType() {
        UUID configUuid = UUID.randomUUID();
        when(processConfigService.getProcessConfig(configUuid)).thenReturn(Optional.of(new PersistedProcessConfig(
            configUuid, new LoadFlowConfig(UUID.randomUUID(), List.of())
        )));

        assertThatThrownBy(() -> service.update(new AutomaticProcessConfigRequest(ProcessType.SECURITY_ANALYSIS, configUuid), "admin"))
            .isInstanceOf(MonitorServerException.class)
            .hasMessageContaining("does not match");

        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void getReturnsStoredConfigUuid() {
        UUID configUuid = UUID.randomUUID();
        AutomaticProcessConfigEntity entity = new AutomaticProcessConfigEntity();
        entity.setProcessType(ProcessType.SECURITY_ANALYSIS);
        entity.setConfigUuid(configUuid);
        when(repository.findById(ProcessType.SECURITY_ANALYSIS)).thenReturn(Optional.of(entity));

        assertThat(service.get(ProcessType.SECURITY_ANALYSIS)).contains(configUuid);
    }

    @Test
    void updateWithSameConfigDoesNotCreateHistoryEntry() {
        UUID configUuid = UUID.randomUUID();
        AutomaticProcessConfigEntity activeConfig = new AutomaticProcessConfigEntity();
        activeConfig.setProcessType(ProcessType.LOADFLOW);
        activeConfig.setConfigUuid(configUuid);
        when(processConfigService.getProcessConfig(configUuid)).thenReturn(Optional.of(new PersistedProcessConfig(
            configUuid, new LoadFlowConfig(UUID.randomUUID(), List.of())
        )));
        when(repository.findById(ProcessType.LOADFLOW)).thenReturn(Optional.of(activeConfig));

        var result = service.update(new AutomaticProcessConfigRequest(ProcessType.LOADFLOW, configUuid), "admin");

        assertThat(result).contains(configUuid);
        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }
}
