/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class AutomaticProcessConfigService {
    private final AutomaticProcessConfigTxService automaticProcessConfigTxService;

    public AutomaticProcessConfigService(AutomaticProcessConfigTxService automaticProcessConfigTxService) {
        this.automaticProcessConfigTxService = automaticProcessConfigTxService;
    }

    public Optional<UUID> update(UUID configUuid, ProcessType processType, String user) {
        return automaticProcessConfigTxService.update(configUuid, processType, user);
    }

    public Optional<UUID> get(ProcessType processType) {
        return automaticProcessConfigTxService.get(processType);
    }
}
