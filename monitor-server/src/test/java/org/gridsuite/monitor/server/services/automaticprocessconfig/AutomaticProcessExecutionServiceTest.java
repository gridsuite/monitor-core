/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessExecutionTriggerType;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.services.processexecution.AutomaticProcessExecutionService;
import org.gridsuite.monitor.server.services.processexecution.ProcessExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class AutomaticProcessExecutionServiceTest {
    @Mock
    private AutomaticProcessConfigTxService configService;
    @Mock
    private ProcessExecutionService processExecutionService;

    private final String automaticUserId = "Automatic";

    @Test
    void launchesEachConfiguredProcessWithAutomaticUser() {
        UUID caseUuid = UUID.randomUUID();
        UUID securityConfigUuid = UUID.randomUUID();
        UUID shortCircuitConfigUuid = UUID.randomUUID();
        UUID loadflowConfigUuid = UUID.randomUUID();
        when(configService.get(ProcessType.SECURITY_ANALYSIS)).thenReturn(Optional.of(securityConfigUuid));
        when(configService.get(ProcessType.SHORT_CIRCUIT)).thenReturn(Optional.of(shortCircuitConfigUuid));
        when(configService.get(ProcessType.LOADFLOW)).thenReturn(Optional.of(loadflowConfigUuid));
        AutomaticProcessExecutionService service = new AutomaticProcessExecutionService(configService, processExecutionService);

        service.executeAutomaticProcesses(caseUuid);

        verify(processExecutionService).executeProcess(caseUuid, automaticUserId, securityConfigUuid, false, ProcessExecutionTriggerType.AUTO);
        verify(processExecutionService).executeProcess(caseUuid, automaticUserId, shortCircuitConfigUuid, false, ProcessExecutionTriggerType.AUTO);
        verify(processExecutionService).executeProcess(caseUuid, automaticUserId, loadflowConfigUuid, false, ProcessExecutionTriggerType.AUTO);
    }

    @Test
    void missingConfigDoesNotPreventOtherProcessesFromLaunching() {
        UUID caseUuid = UUID.randomUUID();
        UUID securityConfigUuid = UUID.randomUUID();
        UUID loadflowConfigUuid = UUID.randomUUID();
        when(configService.get(ProcessType.SECURITY_ANALYSIS)).thenReturn(Optional.of(securityConfigUuid));
        when(configService.get(ProcessType.SHORT_CIRCUIT)).thenReturn(Optional.empty());
        when(configService.get(ProcessType.LOADFLOW)).thenReturn(Optional.of(loadflowConfigUuid));
        AutomaticProcessExecutionService service = new AutomaticProcessExecutionService(configService, processExecutionService);

        service.executeAutomaticProcesses(caseUuid);

        verify(processExecutionService).executeProcess(caseUuid, automaticUserId, securityConfigUuid, false, ProcessExecutionTriggerType.AUTO);
        verify(processExecutionService).executeProcess(caseUuid, automaticUserId, loadflowConfigUuid, false, ProcessExecutionTriggerType.AUTO);
    }
}
