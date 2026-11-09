/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigEntity;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigHistoryEntity;
import org.gridsuite.monitor.server.entities.processconfig.AbstractProcessConfigEntity;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.repositories.automaticprocessconfig.AutomaticProcessConfigHistoryRepository;
import org.gridsuite.monitor.server.repositories.automaticprocessconfig.AutomaticProcessConfigRepository;
import org.gridsuite.monitor.server.repositories.processconfig.ProcessConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class AutomaticProcessConfigTxServiceTest {
    @Mock
    private AutomaticProcessConfigRepository repository;
    @Mock
    private AutomaticProcessConfigHistoryRepository historyRepository;
    @Mock
    private ProcessConfigRepository processConfigRepository;
    @Mock
    private AbstractProcessConfigEntity processConfigEntity;

    private AutomaticProcessConfigTxService service;

    @BeforeEach
    void setUp() {
        service = new AutomaticProcessConfigTxService(repository, historyRepository, processConfigRepository);
    }

    @Test
    void updatePersistsConfigAndAuditInformation() {
        UUID configUuid = UUID.randomUUID();
        String user = "admin";
        when(processConfigRepository.findById(configUuid)).thenReturn(Optional.of(processConfigEntity));
        when(processConfigEntity.getProcessType()).thenReturn(ProcessType.LOADFLOW);
        when(repository.findById(ProcessType.LOADFLOW)).thenReturn(Optional.empty());
        when(repository.save(any(AutomaticProcessConfigEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any(AutomaticProcessConfigHistoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(configUuid, ProcessType.LOADFLOW, user);

        assertThat(result).contains(configUuid);
        verify(repository).save(argThat(saved -> saved.getProcessConfig() == processConfigEntity));
        verify(historyRepository).save(argThat(saved -> saved.getProcessConfig() == processConfigEntity));
        verify(processConfigEntity).getProcessType();
    }

    @Test
    void updateReturnsEmptyWhenProcessConfigDoesNotExist() {
        UUID configUuid = UUID.randomUUID();
        when(processConfigRepository.findById(configUuid)).thenReturn(Optional.empty());

        assertThat(service.update(configUuid, ProcessType.LOADFLOW, "admin")).isEmpty();

        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void updateRejectsProcessConfigWithDifferentType() {
        UUID configUuid = UUID.randomUUID();
        when(processConfigRepository.findById(configUuid)).thenReturn(Optional.of(processConfigEntity));
        when(processConfigEntity.getProcessType()).thenReturn(ProcessType.LOADFLOW);

        assertThatThrownBy(() -> service.update(configUuid, ProcessType.SECURITY_ANALYSIS, "admin"))
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
        when(processConfigEntity.getId()).thenReturn(configUuid);
        entity.setProcessConfig(processConfigEntity);
        when(repository.findById(ProcessType.SECURITY_ANALYSIS)).thenReturn(Optional.of(entity));

        assertThat(service.get(ProcessType.SECURITY_ANALYSIS)).contains(configUuid);
    }

    @Test
    void updateWithSameConfigDoesNotCreateHistoryEntry() {
        UUID configUuid = UUID.randomUUID();
        AutomaticProcessConfigEntity activeConfig = new AutomaticProcessConfigEntity();
        activeConfig.setProcessType(ProcessType.LOADFLOW);
        when(processConfigRepository.findById(configUuid)).thenReturn(Optional.of(processConfigEntity));
        when(processConfigEntity.getId()).thenReturn(configUuid);
        when(processConfigEntity.getProcessType()).thenReturn(ProcessType.LOADFLOW);
        activeConfig.setProcessConfig(processConfigEntity);
        when(repository.findById(ProcessType.LOADFLOW)).thenReturn(Optional.of(activeConfig));

        var result = service.update(configUuid, ProcessType.LOADFLOW, "admin");

        assertThat(result).contains(configUuid);
        verify(repository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }
}
