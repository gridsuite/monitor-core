/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.AbstractProcessConfigEntity;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mapper.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ProcessConfigService {
    private final ProcessConfigRepository processConfigRepository;

    private final SecurityAnalysisConfigMapper securityAnalysisConfigMapper;

    public ProcessConfigService(ProcessConfigRepository processConfigRepository, SecurityAnalysisConfigMapper securityAnalysisConfigMapper) {
        this.processConfigRepository = processConfigRepository;
        this.securityAnalysisConfigMapper = securityAnalysisConfigMapper;
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        AbstractProcessConfigEntity entity = switch (processConfig) {
            case SecurityAnalysisConfig sac -> securityAnalysisConfigMapper.toEntity(sac);
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
        };
        return processConfigRepository.save(entity).getId();
    }

    @Transactional(readOnly = true)
    public Optional<ProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid).flatMap(entity -> switch (entity) {
            case SecurityAnalysisConfigEntity sae ->
                Optional.of((ProcessConfig) securityAnalysisConfigMapper.toDto(sae));
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entity.getType());
        });
    }

    @Transactional
    public boolean updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        return processConfigRepository.findById(processConfigUuid)
            .map(entity -> {
                if (entity.getType() != processConfig.processType()) {
                    throw new IllegalArgumentException("Process config type mismatch : " + entity.getType());
                }
                switch (processConfig) {
                    case SecurityAnalysisConfig sac ->
                        securityAnalysisConfigMapper.update((SecurityAnalysisConfigEntity) entity, sac);
                    default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
                }
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public boolean deleteProcessConfig(UUID processConfigUuid) {
        if (processConfigRepository.existsById(processConfigUuid)) {
            processConfigRepository.deleteById(processConfigUuid);
            return true;
        }
        return false;
    }
}
