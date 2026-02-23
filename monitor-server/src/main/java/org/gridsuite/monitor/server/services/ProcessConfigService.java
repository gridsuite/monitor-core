/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.PersistedProcessConfig;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mapper.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.gridsuite.monitor.server.repositories.SecurityAnalysisConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ProcessConfigService {
    private final ProcessConfigRepository processConfigRepository;
    private final SecurityAnalysisConfigRepository securityAnalysisConfigRepository;

    public ProcessConfigService(ProcessConfigRepository processConfigRepository,
                                SecurityAnalysisConfigRepository securityAnalysisConfigRepository) {
        this.processConfigRepository = processConfigRepository;
        this.securityAnalysisConfigRepository = securityAnalysisConfigRepository;
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        switch (processConfig) {
            case SecurityAnalysisConfig sac -> {
                return securityAnalysisConfigRepository.save(SecurityAnalysisConfigMapper.toEntity(sac)).getId();
            }
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
        }
    }

    @Transactional(readOnly = true)
    public Optional<PersistedProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid).flatMap(entity -> switch (entity) {
            case SecurityAnalysisConfigEntity sae -> Optional.of(SecurityAnalysisConfigMapper.toDto(sae));
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
                        SecurityAnalysisConfigMapper.update((SecurityAnalysisConfigEntity) entity, sac);
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

    @Transactional(readOnly = true)
    public List<PersistedProcessConfig> getProcessConfigs(ProcessType processType) {
        return switch (processType) {
            case SECURITY_ANALYSIS -> securityAnalysisConfigRepository.findAll().stream().map(SecurityAnalysisConfigMapper::toDto).toList();
        };
    }
}
