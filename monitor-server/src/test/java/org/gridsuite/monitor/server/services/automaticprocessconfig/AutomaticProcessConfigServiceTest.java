/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

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

    private AutomaticProcessConfigService automaticProcessConfigService;

    @BeforeEach
    void setUp() {
        automaticProcessConfigService = new AutomaticProcessConfigService(automaticProcessConfigTxService);
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
}
