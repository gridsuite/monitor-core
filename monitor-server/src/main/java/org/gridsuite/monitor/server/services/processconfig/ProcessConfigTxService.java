/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.MetadataInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProcessConfigTxService {
    private final ProcessConfigRepository processConfigRepository;
    private final SecurityAnalysisConfigMapper securityAnalysisConfigMapper;
    private final LoadFlowConfigMapper loadFlowConfigMapper;

    public ProcessConfigTxService(ProcessConfigRepository processConfigRepository,
                                  SecurityAnalysisConfigMapper securityAnalysisConfigMapper,
                                  LoadFlowConfigMapper loadFlowConfigMapper) {
        this.processConfigRepository = processConfigRepository;
        this.securityAnalysisConfigMapper = securityAnalysisConfigMapper;
        this.loadFlowConfigMapper = loadFlowConfigMapper;
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        return createProcessConfigEntity(processConfig);
    }

    public Optional<PersistedProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid).map(this::toPersistedProcessConfig);
    }

    public List<MetadataInfos> getProcessConfigsMetadata(List<UUID> processConfigUuids) {
        return processConfigRepository.findAllById(processConfigUuids).stream()
            .map(entity -> new MetadataInfos(entity.getId(), entity.getProcessType()))
            .toList();
    }

    @Transactional
    public Optional<UUID> updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        return processConfigRepository.findById(processConfigUuid)
            .map(entity -> {
                if (entity.getProcessType() != processConfig.processType()) {
                    throw new IllegalArgumentException("Process config type mismatch : " + entity.getProcessType());
                }
                switch (processConfig) {
                    case SecurityAnalysisConfig sac ->
                        securityAnalysisConfigMapper.updateEntityFromDto(sac, (SecurityAnalysisConfigEntity) entity);
                    case LoadFlowConfig lfc ->
                        loadFlowConfigMapper.updateEntityFromDto(lfc, (LoadFlowConfigEntity) entity);
                    default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
                }
                return processConfigUuid;
            });
    }

    @Transactional
    public Optional<UUID> duplicateProcessConfig(UUID sourceProcessConfigUuid) {
        return processConfigRepository.findById(sourceProcessConfigUuid)
            .map(sourceEntity -> createProcessConfigEntity(toProcessConfig(sourceEntity)));
    }

    @Transactional
    public Optional<UUID> deleteProcessConfig(UUID processConfigUuid) {
        if (processConfigRepository.existsById(processConfigUuid)) {
            processConfigRepository.deleteById(processConfigUuid);
            return Optional.of(processConfigUuid);
        } else {
            return Optional.empty();
        }
    }

    public List<PersistedProcessConfig> getProcessConfigs(ProcessType processType) {
        return processConfigRepository.findAllByProcessType(processType).stream()
            .map(this::toPersistedProcessConfig)
            .toList();
    }

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
            case LoadFlowConfig lfc1 -> compareLoadFlowConfigs(lfc1, (LoadFlowConfig) processConfig2);
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig1.processType());
        };

        boolean identical = differences.stream().allMatch(ProcessConfigFieldComparison::identical);

        return Optional.of(new ProcessConfigComparison(uuid1, uuid2, identical, differences));
    }

    private UUID createProcessConfigEntity(ProcessConfig processConfig) {
        switch (processConfig) {
            case SecurityAnalysisConfig sac -> {
                return processConfigRepository.save(securityAnalysisConfigMapper.toEntity(sac)).getId();
            }
            case LoadFlowConfig lfc -> {
                return processConfigRepository.save(loadFlowConfigMapper.toEntity(lfc)).getId();
            }
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
        }
    }

    private ProcessConfig toProcessConfig(ProcessConfigEntity entity) {
        return switch (entity) {
            case SecurityAnalysisConfigEntity sae -> securityAnalysisConfigMapper.toDto(sae);
            case LoadFlowConfigEntity lfe -> loadFlowConfigMapper.toDto(lfe);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entity.getProcessType());
        };
    }

    private PersistedProcessConfig toPersistedProcessConfig(ProcessConfigEntity entity) {
        return new PersistedProcessConfig(entity.getId(), toProcessConfig(entity));
    }

    private void compareConfigsModifications(ProcessConfig config1, ProcessConfig config2, List<ProcessConfigFieldComparison> differences) {
        boolean modificationsIdentical = Objects.equals(config1.modificationUuids(), config2.modificationUuids());
        differences.add(new ProcessConfigFieldComparison(
            "modifications",
            modificationsIdentical,
            config1.modificationUuids(),
            config2.modificationUuids()
        ));
    }

    private List<ProcessConfigFieldComparison> compareSecurityAnalysisConfigs(SecurityAnalysisConfig config1, SecurityAnalysisConfig config2) {
        List<ProcessConfigFieldComparison> differences = new ArrayList<>();

        compareConfigsModifications(config1, config2, differences);

        boolean securityAnalysisParametersIdentical = Objects.equals(config1.securityAnalysisParametersUuid(), config2.securityAnalysisParametersUuid());
        differences.add(new ProcessConfigFieldComparison(
            "securityAnalysisParameters",
            securityAnalysisParametersIdentical,
            config1.securityAnalysisParametersUuid(),
            config2.securityAnalysisParametersUuid()
        ));

        boolean loadflowParametersIdentical = Objects.equals(config1.loadflowParametersUuid(), config2.loadflowParametersUuid());
        differences.add(new ProcessConfigFieldComparison(
            "loadflowParameters",
            loadflowParametersIdentical,
            config1.loadflowParametersUuid(),
            config2.loadflowParametersUuid()
        ));

        return differences;
    }

    private List<ProcessConfigFieldComparison> compareLoadFlowConfigs(LoadFlowConfig config1, LoadFlowConfig config2) {
        List<ProcessConfigFieldComparison> differences = new ArrayList<>();

        compareConfigsModifications(config1, config2, differences);

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
