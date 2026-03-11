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

    public ProcessConfigService(ProcessConfigRepository processConfigRepository) {
        this.processConfigRepository = processConfigRepository;
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        switch (processConfig) {
            case SecurityAnalysisConfig sac -> {
                return processConfigRepository.save(SecurityAnalysisConfigMapper.toEntity(sac)).getId();
            }
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
        }
    }

    @Transactional(readOnly = true)
    public Optional<PersistedProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid).map(this::toPersistedProcessConfig);
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
        return processConfigRepository.findAllByProcessType(processType).stream()
            .map(this::toPersistedProcessConfig)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProcessConfigComparison> compareProcessConfigs(UUID uuid1, UUID uuid2) {
        Optional<ProcessConfigEntity> processConfigEntity1 = processConfigRepository.findById(uuid1);
        Optional<ProcessConfigEntity> processConfigEntity2 = processConfigRepository.findById(uuid2);

        if (processConfigEntity1.isEmpty() || processConfigEntity2.isEmpty()) {
            return Optional.empty();
        }

        ProcessConfig processConfig1 = toProcessConfig(processConfigEntity1.get());
        ProcessConfig processConfig2 = toProcessConfig(processConfigEntity2.get());

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

    private PersistedProcessConfig toPersistedProcessConfig(ProcessConfigEntity entity) {
        return switch (entity) {
            case SecurityAnalysisConfigEntity sae -> SecurityAnalysisConfigMapper.toDto(sae);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entity.getProcessType());
        };
    }

    private ProcessConfig toProcessConfig(ProcessConfigEntity entity) {
        return toPersistedProcessConfig(entity).processConfig();
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

        // Compare parameters
        boolean parametersIdentical = Objects.equals(config1.parametersUuid(), config2.parametersUuid());
        differences.add(new ProcessConfigFieldComparison(
            "securityAnalysisParameters",
            parametersIdentical,
            config1.parametersUuid(),
            config2.parametersUuid()
        ));

        // Compare contingencies
        boolean contingenciesIdentical = Objects.equals(config1.contingencies(), config2.contingencies());
        differences.add(new ProcessConfigFieldComparison(
            "contingencies",
            contingenciesIdentical,
            config1.contingencies(),
            config2.contingencies()
        ));

        return differences;
    }
}
