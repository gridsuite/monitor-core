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
import org.gridsuite.monitor.server.entities.ProcessConfigEntity;
import org.gridsuite.monitor.server.dto.ProcessConfigComparison;
import org.gridsuite.monitor.server.dto.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mapper.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        switch (processConfig) {
            case SecurityAnalysisConfig sac -> {
                return processConfigRepository.save(securityAnalysisConfigMapper.toEntity(sac)).getId();
            }
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
        }
    }

    @Transactional(readOnly = true)
    public Optional<PersistedProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid).flatMap(entity -> switch (entity) {
            case SecurityAnalysisConfigEntity sae -> Optional.of(securityAnalysisConfigMapper.toPersistedProcessConfigDto(sae));
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entity.getProcessType());
        });
    }

    @Transactional
    public boolean updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        return processConfigRepository.findById(processConfigUuid)
            .map(entity -> {
                if (entity.getProcessType() != processConfig.processType()) {
                    throw new IllegalArgumentException("Process config type mismatch : " + entity.getProcessType());
                }
                switch (processConfig) {
                    case SecurityAnalysisConfig sac ->
                        securityAnalysisConfigMapper.updateEntityFromDto(sac, (SecurityAnalysisConfigEntity) entity);
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
        List<ProcessConfigEntity> processConfigs = processConfigRepository.findAllByProcessType(processType);
        return processConfigs.stream().map(entity -> switch (entity) {
            case SecurityAnalysisConfigEntity sae -> securityAnalysisConfigMapper.toPersistedProcessConfigDto(sae);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entity.getProcessType());
        }).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProcessConfigComparison> compareProcessConfigs(UUID uuid1, UUID uuid2) {
        Optional<PersistedProcessConfig> config1 = getProcessConfig(uuid1);
        Optional<PersistedProcessConfig> config2 = getProcessConfig(uuid2);

        if (config1.isEmpty() || config2.isEmpty()) {
            return Optional.empty();
        }

        ProcessConfig processConfig1 = config1.get().processConfig();
        ProcessConfig processConfig2 = config2.get().processConfig();

        if (processConfig1.processType() != processConfig2.processType()) {
            throw new IllegalArgumentException("Cannot compare different process config types: " + processConfig1.processType() + " vs " + processConfig2.processType());
        }

        List<ProcessConfigFieldComparison> differences = switch (processConfig1) {
            case SecurityAnalysisConfig sac1 -> compareSecurityAnalysisConfigs(sac1, (SecurityAnalysisConfig) processConfig2);
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig1.processType());
        };

        boolean identical = differences.stream().allMatch(ProcessConfigFieldComparison::identical);

        return Optional.of(new ProcessConfigComparison(uuid1, uuid2, identical, differences));
    }

    private List<ProcessConfigFieldComparison> compareSecurityAnalysisConfigs(SecurityAnalysisConfig config1, SecurityAnalysisConfig config2) {
        List<ProcessConfigFieldComparison> differences = new ArrayList<>();

        // Compare modifications
        boolean modificationsIdentical = Objects.equals(config1.modificationUuids(), config2.modificationUuids());
        differences.add(new ProcessConfigFieldComparison(
            "modifications",
            modificationsIdentical,
            config1.modificationUuids(),
            config2.modificationUuids()
        ));

        // Compare security analysis parameters
        boolean securityAnalysisParametersIdentical = Objects.equals(config1.securityAnalysisParametersUuid(), config2.securityAnalysisParametersUuid());
        differences.add(new ProcessConfigFieldComparison(
            "securityAnalysisParameters",
            securityAnalysisParametersIdentical,
            config1.securityAnalysisParametersUuid(),
            config2.securityAnalysisParametersUuid()
        ));

        // Compare loadflow parameters
        boolean loadflowParametersIdentical = Objects.equals(config1.loadflowParametersUuid(), config2.loadflowParametersUuid());
        differences.add(new ProcessConfigFieldComparison(
            "loadflowParameters",
            loadflowParametersIdentical,
            config1.loadflowParametersUuid(),
            config2.loadflowParametersUuid()
        ));

        return differences;
    }
}
