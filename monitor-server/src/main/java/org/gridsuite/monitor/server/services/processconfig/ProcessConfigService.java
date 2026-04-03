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
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.entities.processconfig.AbstractProcessConfigEntity;
import org.gridsuite.monitor.server.repositories.processconfig.ProcessConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ProcessConfigService {
    private final ProcessConfigRepository processConfigRepository;
    private final Map<ProcessType, ProcessConfigHandler<ProcessConfig, AbstractProcessConfigEntity>> handlers;

    @SuppressWarnings("unchecked")
    public ProcessConfigService(ProcessConfigRepository processConfigRepository,
                                List<ProcessConfigHandler<?, ?>> handlers) {
        this.processConfigRepository = processConfigRepository;
        this.handlers = handlers.stream().collect(Collectors.toMap(
            ProcessConfigHandler::getProcessType,
            h -> (ProcessConfigHandler<ProcessConfig, AbstractProcessConfigEntity>) h
        ));
    }

    private ProcessConfigHandler<ProcessConfig, AbstractProcessConfigEntity> getHandler(ProcessType processType) {
        ProcessConfigHandler<ProcessConfig, AbstractProcessConfigEntity> handler = handlers.get(processType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported process config type: " + processType);
        }
        return handler;
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        return processConfigRepository.save(getHandler(processConfig.processType()).toEntity(processConfig)).getId();
    }

    @Transactional(readOnly = true)
    public Optional<PersistedProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid).map(this::toPersistedProcessConfig);
    }

    @Transactional(readOnly = true)
    public List<MetadataInfos> getProcessConfigsMetadata(List<UUID> processConfigUuids) {
        return processConfigRepository.findAllById(processConfigUuids).stream()
            .map(entity -> new MetadataInfos(entity.getId(), entity.getProcessType()))
            .toList();
    }

    @Transactional
    public boolean updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        return processConfigRepository.findById(processConfigUuid)
            .map(entity -> {
                if (entity.getProcessType() != processConfig.processType()) {
                    throw new IllegalArgumentException("Process config type mismatch : " + entity.getProcessType());
                }
                getHandler(processConfig.processType()).updateEntity(processConfig, entity);
                return true;
            })
            .orElse(false);
    }

    @Transactional
    public Optional<UUID> duplicateProcessConfig(UUID sourceProcessConfigUuid) {
        return processConfigRepository.findById(sourceProcessConfigUuid)
            .map(sourceEntity -> {
                ProcessConfig sourceConfig = toProcessConfig(sourceEntity);
                return processConfigRepository.save(getHandler(sourceConfig.processType()).toEntity(sourceConfig)).getId();
            });
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
        return getHandler(processType).findAll().stream()
            .map(this::toPersistedProcessConfig)
            .toList();
    }

    private ProcessConfig toProcessConfig(AbstractProcessConfigEntity entity) {
        return getHandler(entity.getProcessType()).toDto(entity);
    }

    private PersistedProcessConfig toPersistedProcessConfig(AbstractProcessConfigEntity entity) {
        return new PersistedProcessConfig(entity.getId(), toProcessConfig(entity));
    }

    @Transactional(readOnly = true)
    public Optional<ProcessConfigComparison> compareProcessConfigs(UUID uuid1, UUID uuid2) {
        Optional<AbstractProcessConfigEntity> processConfigEntity1 = processConfigRepository.findById(uuid1);
        Optional<AbstractProcessConfigEntity> processConfigEntity2 = processConfigRepository.findById(uuid2);

        if (processConfigEntity1.isEmpty() || processConfigEntity2.isEmpty()) {
            return Optional.empty();
        }

        ProcessConfig processConfig1 = toProcessConfig(processConfigEntity1.get());
        ProcessConfig processConfig2 = toProcessConfig(processConfigEntity2.get());

        if (processConfig1.processType() != processConfig2.processType()) {
            throw new IllegalArgumentException("Cannot compare different process config types: " + processConfig1.processType() + " vs " + processConfig2.processType());
        }

        List<ProcessConfigFieldComparison> differences = getHandler(processConfig1.processType()).compare(processConfig1, processConfig2);
        boolean identical = differences.stream().allMatch(ProcessConfigFieldComparison::identical);

        return Optional.of(new ProcessConfigComparison(uuid1, uuid2, identical, differences));
    }
}
