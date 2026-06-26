/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.MetadataInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.DIFFERENT_PROCESS_CONFIG_TYPE;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.PROCESS_CONFIG_NOT_FOUND;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ProcessConfigService {
    private final ProcessConfigRepository processConfigRepository;
    private final Map<ProcessType, ProcessConfigHandler<?, ?>> processConfigHandlers;

    public ProcessConfigService(ProcessConfigRepository processConfigRepository,
                                List<ProcessConfigHandler<?, ?>> listHandlers) {
        this.processConfigRepository = processConfigRepository;
        this.processConfigHandlers = listHandlers.stream().collect(Collectors.toMap(
            ProcessConfigHandler::getProcessType, Function.identity(), (h1, h2) -> {
                throw new IllegalStateException(String.format("Duplicate process config handlers for process type: %s", h1.getProcessType()));
            }
        ));
    }

    @SuppressWarnings("unchecked")
    private <C extends ProcessConfig, E extends ProcessConfigEntity> ProcessConfigHandler<C, E> getHandler(ProcessType processType) {
        ProcessConfigHandler<?, ?> handler = processConfigHandlers.get(processType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported process config type: " + processType);
        }
        return (ProcessConfigHandler<C, E>) handler;
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        ProcessConfigEntity entity = getHandler(processConfig.processType()).toEntity(processConfig);
        return processConfigRepository.save(entity).getId();
    }

    @Transactional(readOnly = true)
    public PersistedProcessConfig getProcessConfig(UUID processConfigUuid) {
        return toPersistedProcessConfig(getProcessConfigEntity(processConfigUuid));
    }

    @Transactional(readOnly = true)
    public List<PersistedProcessConfig> getProcessConfigs(ProcessType processType) {
        return processConfigRepository.findAllByProcessType(processType).stream()
            .map(this::toPersistedProcessConfig)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MetadataInfos> getProcessConfigsMetadata(List<UUID> processConfigUuids) {
        return processConfigRepository.findAllById(processConfigUuids).stream()
            .map(entity -> new MetadataInfos(entity.getId(), entity.getProcessType()))
            .toList();
    }

    @Transactional
    public void updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        ProcessConfigEntity entity = getProcessConfigEntity(processConfigUuid);
        if (entity.getProcessType() != processConfig.processType()) {
            throw new IllegalArgumentException("Process config type mismatch : " + entity.getProcessType());
        }
        getHandler(processConfig.processType()).update(processConfig, entity);
    }

    @Transactional
    public UUID duplicateProcessConfig(UUID sourceProcessConfigUuid) {
        ProcessConfigEntity sourceEntity = getProcessConfigEntity(sourceProcessConfigUuid);
        ProcessConfigEntity entity = getHandler(sourceEntity.getProcessType()).copyEntity(sourceEntity);
        return processConfigRepository.save(entity).getId();
    }

    @Transactional
    public void deleteProcessConfig(UUID processConfigUuid) {
        if (processConfigRepository.existsById(processConfigUuid)) {
            processConfigRepository.deleteById(processConfigUuid);
            return;
        }
        throw processConfigNotFound(processConfigUuid);
    }

    private PersistedProcessConfig toPersistedProcessConfig(ProcessConfigEntity entity) {
        return new PersistedProcessConfig(entity.getId(), toProcessConfig(entity));
    }

    private ProcessConfig toProcessConfig(ProcessConfigEntity entity) {
        return getHandler(entity.getProcessType()).toDto(entity);
    }

    @Transactional(readOnly = true)
    public ProcessConfigComparison compareProcessConfigs(UUID uuid1, UUID uuid2) {
        ProcessConfigEntity processConfigEntity1 = getProcessConfigEntity(uuid1);
        ProcessConfigEntity processConfigEntity2 = getProcessConfigEntity(uuid2);

        if (processConfigEntity1.getProcessType() != processConfigEntity2.getProcessType()) {
            throw new MonitorServerException(DIFFERENT_PROCESS_CONFIG_TYPE, "Cannot compare different process config types",
                Map.of("processConfigEntity1Type", processConfigEntity1.getProcessType(), "processConfigEntity2Type", processConfigEntity2.getProcessType()));
        }

        ProcessConfig processConfig1 = toProcessConfig(processConfigEntity1);
        ProcessConfig processConfig2 = toProcessConfig(processConfigEntity2);

        List<ProcessConfigFieldComparison> differences = processConfig1.compareWith(processConfig2);
        boolean identical = differences.stream().allMatch(ProcessConfigFieldComparison::identical);

        return new ProcessConfigComparison(uuid1, uuid2, identical, differences);
    }

    private ProcessConfigEntity getProcessConfigEntity(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid)
            .orElseThrow(() -> processConfigNotFound(processConfigUuid));
    }

    private MonitorServerException processConfigNotFound(UUID processConfigUuid) {
        return new MonitorServerException(PROCESS_CONFIG_NOT_FOUND, "Process config not found",
            Map.of("processConfigUuid", processConfigUuid));
    }
}
