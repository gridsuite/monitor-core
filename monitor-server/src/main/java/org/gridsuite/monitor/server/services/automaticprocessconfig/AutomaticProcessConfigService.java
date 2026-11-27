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
import org.gridsuite.monitor.server.messaging.NotificationService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class AutomaticProcessConfigService {
    private final AutomaticProcessConfigTxService automaticProcessConfigTxService;
    private final DirectoryRestClient directoryRestClient;
    private final NotificationService notificationService;

    public AutomaticProcessConfigService(AutomaticProcessConfigTxService automaticProcessConfigTxService,
                                         DirectoryRestClient directoryRestClient, NotificationService notificationService) {
        this.automaticProcessConfigTxService = automaticProcessConfigTxService;
        this.directoryRestClient = directoryRestClient;
        this.notificationService = notificationService;
    }

    public Optional<UUID> update(UUID configUuid, ProcessType processType, String user) {
        Optional<UUID> result = automaticProcessConfigTxService.update(configUuid, processType, user);
        if (result.isPresent()) {
            notificationService.sendAutomaticProcessConfigUpdateMessage();
        }
        return result;
    }

    public Optional<UUID> get(ProcessType processType) {
        return automaticProcessConfigTxService.get(processType);
    }

    public List<AutomaticProcessConfigInfos> getHistory() {
        return withDirectoryNames(automaticProcessConfigTxService.getHistory());
    }

    public List<AutomaticProcessConfigInfos> getActives() {
        return withDirectoryNames(automaticProcessConfigTxService.getActives());
    }

    private List<AutomaticProcessConfigInfos> withDirectoryNames(List<AutomaticProcessConfigInfos> infos) {
        if (infos.isEmpty()) {
            return infos;
        }
        List<UUID> processConfigUuids = infos.stream().map(AutomaticProcessConfigInfos::processConfigUuid).distinct().toList();
        Map<UUID, String> processConfigNamesMap = Objects.requireNonNullElse(directoryRestClient.getElementNames(processConfigUuids), Collections.emptyMap());
        return infos.stream()
            .map(info -> info.withProcessConfigName(processConfigNamesMap.get(info.processConfigUuid())))
            .toList();
    }
}
