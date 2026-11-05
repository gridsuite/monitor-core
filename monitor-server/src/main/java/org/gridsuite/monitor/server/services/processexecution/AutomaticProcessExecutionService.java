/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import org.gridsuite.monitor.commons.types.processexecution.ProcessExecutionTriggerType;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.services.automaticprocessconfig.AutomaticProcessConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@Service
public class AutomaticProcessExecutionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticProcessExecutionService.class);
    private static final String AUTOMATIC_USER_ID = "Automatic";
    private static final List<ProcessType> PROCESS_TYPES = List.of(
        ProcessType.SECURITY_ANALYSIS,
        ProcessType.SHORT_CIRCUIT,
        ProcessType.LOADFLOW
    );

    private final AutomaticProcessConfigService configService;
    private final ProcessExecutionService processExecutionService;

    public AutomaticProcessExecutionService(AutomaticProcessConfigService configService, ProcessExecutionService processExecutionService) {
        this.configService = configService;
        this.processExecutionService = processExecutionService;
    }

    public void executeAutomaticProcesses(UUID caseUuid) {
        for (ProcessType processType : PROCESS_TYPES) {
            Optional<UUID> configUuid = configService.get(processType);
            if (configUuid.isEmpty()) {
                LOGGER.warn("No automatic execution config found for process type {}", processType);
                continue;
            }
            processExecutionService.executeProcess(caseUuid, AUTOMATIC_USER_ID, configUuid.get(), false, ProcessExecutionTriggerType.AUTO);
        }
    }
}
