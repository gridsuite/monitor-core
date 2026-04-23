/**
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ProcessConfigService {
    private final ProcessConfigTxService processConfigTxService;

    public ProcessConfigService(ProcessConfigTxService processConfigTxService) {
        this.processConfigTxService = processConfigTxService;
    }

    public UUID createProcessConfig(ProcessConfig processConfig) {
        return processConfigTxService.createProcessConfig(processConfig);
    }

    public Optional<PersistedProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigTxService.getProcessConfig(processConfigUuid);
    }

    public List<MetadataInfos> getProcessConfigsMetadata(List<UUID> processConfigUuids) {
        return processConfigTxService.getProcessConfigsMetadata(processConfigUuids);
    }

    public Optional<UUID> updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        return processConfigTxService.updateProcessConfig(processConfigUuid, processConfig);
    }

    public Optional<UUID> duplicateProcessConfig(UUID sourceProcessConfigUuid) {
        return processConfigTxService.duplicateProcessConfig(sourceProcessConfigUuid);
    }

    public Optional<UUID> deleteProcessConfig(UUID processConfigUuid) {
        return processConfigTxService.deleteProcessConfig(processConfigUuid);
    }

    public List<PersistedProcessConfig> getProcessConfigs(ProcessType processType) {
        return processConfigTxService.getProcessConfigs(processType);
    }

    public Optional<ProcessConfigComparison> compareProcessConfigs(UUID uuid1, UUID uuid2) {
        return processConfigTxService.compareProcessConfigs(uuid1, uuid2);
    }
}
