/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.clients.DirectoryRestClient;
import org.gridsuite.monitor.server.dto.processconfig.AutomaticProcessConfigInfos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@ExtendWith(MockitoExtension.class)
class AutomaticProcessConfigServiceTest {
    @Mock
    private AutomaticProcessConfigTxService automaticProcessConfigTxService;

    @Mock
    private DirectoryRestClient directoryRestClient;

    private AutomaticProcessConfigService automaticProcessConfigService;

    @BeforeEach
    void setUp() {
        automaticProcessConfigService = new AutomaticProcessConfigService(automaticProcessConfigTxService, directoryRestClient);
    }

    @Test
    void update() {
        UUID configUuid = UUID.randomUUID();
        Optional<UUID> expected = Optional.of(configUuid);
        when(automaticProcessConfigTxService.update(configUuid, ProcessType.LOADFLOW, "admin")).thenReturn(expected);

        assertThat(automaticProcessConfigService.update(configUuid, ProcessType.LOADFLOW, "admin")).isEqualTo(expected);

        verify(automaticProcessConfigTxService).update(configUuid, ProcessType.LOADFLOW, "admin");
    }

    @Test
    void get() {
        UUID configUuid = UUID.randomUUID();
        Optional<UUID> expected = Optional.of(configUuid);
        when(automaticProcessConfigTxService.get(ProcessType.SECURITY_ANALYSIS)).thenReturn(expected);

        assertThat(automaticProcessConfigService.get(ProcessType.SECURITY_ANALYSIS)).isEqualTo(expected);

        verify(automaticProcessConfigTxService).get(ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void getActivesWithNames() {
        UUID configUuid = UUID.randomUUID();
        AutomaticProcessConfigInfos infos = new AutomaticProcessConfigInfos(configUuid, null, ProcessType.LOADFLOW, Instant.now(), "admin");
        when(automaticProcessConfigTxService.getActives()).thenReturn(List.of(infos));
        when(directoryRestClient.getElementNames(List.of(configUuid))).thenReturn(Map.of(configUuid, "my config"));

        assertThat(automaticProcessConfigService.getActives())
            .containsExactly(infos.withProcessConfigName("my config"));
    }

    @Test
    void getHistoryWithNames() {
        UUID configUuid = UUID.randomUUID();
        AutomaticProcessConfigInfos infos = new AutomaticProcessConfigInfos(configUuid, null, ProcessType.LOADFLOW, Instant.now(), "admin");
        when(automaticProcessConfigTxService.getHistory()).thenReturn(List.of(infos, infos));
        when(directoryRestClient.getElementNames(List.of(configUuid))).thenReturn(Map.of(configUuid, "my config"));

        assertThat(automaticProcessConfigService.getHistory())
            .containsExactly(infos.withProcessConfigName("my config"), infos.withProcessConfigName("my config"));
    }
}
