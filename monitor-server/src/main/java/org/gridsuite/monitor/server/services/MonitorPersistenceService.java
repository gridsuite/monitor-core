/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.PersistedProcessConfig;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.gridsuite.monitor.server.utils.S3PathResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class MonitorPersistenceService {

    private final ProcessExecutionRepository executionRepository;
    private final ProcessConfigService processConfigService;
    private final S3PathResolver s3PathResolver;

    public record CreatedExecution(UUID executionId, ProcessConfig processConfig, String debugFileLocation) {
    }

    public record DeletedExecution(List<ResultInfos> resultInfos, List<UUID> reportIds) {
    }

    @Transactional
    public Optional<CreatedExecution> createExecution(UUID caseUuid, String userId, UUID processConfigId, boolean isDebug) {
        UUID executionId = UUID.randomUUID();
        return processConfigService.getProcessConfig(processConfigId)
            .map(persistedProcessConfig -> createExecution(caseUuid, userId, isDebug, executionId, persistedProcessConfig));
    }

    @Transactional
    public Optional<DeletedExecution> deleteExecution(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(execution -> {
                DeletedExecution deletedExecution = new DeletedExecution(getResultInfos(execution), getReportIds(execution));
                executionRepository.delete(execution);
                return deletedExecution;
            });
    }

    private CreatedExecution createExecution(UUID caseUuid, String userId, boolean isDebug, UUID executionId, PersistedProcessConfig persistedProcessConfig) {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .id(executionId)
            .type(persistedProcessConfig.processConfig().processType().name())
            .caseUuid(caseUuid)
            .processConfigId(persistedProcessConfig.id())
            .status(ProcessStatus.SCHEDULED)
            .scheduledAt(Instant.now())
            .userId(userId)
            .debugFileLocation(getDebugFileLocation(isDebug, persistedProcessConfig, executionId))
            .build();
        executionRepository.save(execution);
        return new CreatedExecution(execution.getId(), persistedProcessConfig.processConfig(), execution.getDebugFileLocation());
    }

    private String getDebugFileLocation(boolean isDebug, PersistedProcessConfig persistedProcessConfig, UUID executionId) {
        return isDebug ? s3PathResolver.toDebugLocation(persistedProcessConfig.processConfig().processType().name(), executionId) : null;
    }

    private List<ResultInfos> getResultInfos(ProcessExecutionEntity execution) {
        return Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
            .filter(step -> step.getResultId() != null && step.getResultType() != null)
            .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
            .toList();
    }

    private List<UUID> getReportIds(ProcessExecutionEntity execution) {
        return Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
            .map(ProcessExecutionStepEntity::getReportId)
            .filter(Objects::nonNull)
            .toList();
    }
}
