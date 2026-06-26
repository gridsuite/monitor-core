/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.dto.processexecution.ProcessExecution;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.mappers.processexecution.ProcessExecutionMapper;
import org.gridsuite.monitor.server.mappers.processexecution.ProcessExecutionStepMapper;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.gridsuite.monitor.server.services.processconfig.ProcessConfigService;
import org.gridsuite.monitor.server.utils.S3PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.DEBUG_INFOS_NOT_FOUND;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.PROCESS_EXECUTION_NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class ProcessExecutionTxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutionTxService.class);

    private final ProcessExecutionRepository processExecutionRepository;
    private final ProcessExecutionStepMapper processExecutionStepMapper;
    private final ProcessExecutionMapper processExecutionMapper;
    private final ProcessConfigService processConfigService;
    private final S3PathResolver s3PathResolver;

    public ProcessExecutionTxService(ProcessExecutionRepository processExecutionRepository,
                                     ProcessExecutionStepMapper processExecutionStepMapper,
                                     ProcessExecutionMapper processExecutionMapper,
                                     ProcessConfigService processConfigService,
                                     S3PathResolver s3PathResolver) {
        this.processExecutionRepository = processExecutionRepository;
        this.processExecutionStepMapper = processExecutionStepMapper;
        this.processExecutionMapper = processExecutionMapper;
        this.processConfigService = processConfigService;
        this.s3PathResolver = s3PathResolver;
    }

    @Transactional
    public ProcessCreationResult createExecution(UUID caseUuid, String userId, UUID processConfigId, UUID executionId, UUID reportId, boolean isDebug) {
        PersistedProcessConfig persistedProcessConfig = processConfigService.getProcessConfig(processConfigId);

        String debugFileLocation = isDebug
            ? s3PathResolver.toDebugLocation(persistedProcessConfig.processConfig().processType().name(), executionId)
            : null;

        processExecutionRepository.save(ProcessExecutionEntity.builder()
            .id(executionId)
            .type(persistedProcessConfig.processConfig().processType().name())
            .caseUuid(caseUuid)
            .processConfigId(persistedProcessConfig.id())
            .status(ProcessStatus.SCHEDULED)
            .scheduledAt(Instant.now())
            .reportId(reportId)
            .userId(userId)
            .debugFileLocation(debugFileLocation)
            .build());

        return new ProcessCreationResult(
            debugFileLocation,
            persistedProcessConfig.processConfig()
        );
    }

    @Transactional
    public void updateExecutionStatus(UUID executionId, ProcessStatus status, String executionEnvName, Instant startedAt, Instant completedAt) {
        processExecutionRepository.findById(executionId).ifPresentOrElse(execution -> {
            execution.setStatus(status);
            if (executionEnvName != null) {
                execution.setExecutionEnvName(executionEnvName);
            }
            if (startedAt != null) {
                execution.setStartedAt(startedAt);
            }
            if (completedAt != null) {
                execution.setCompletedAt(completedAt);
            }
            processExecutionRepository.save(execution);
        }, () -> LOGGER.warn("Execution {} not found in DB, ignoring status update", executionId));
    }

    @Transactional
    public void updateStepStatus(UUID executionId, ProcessExecutionStep processExecutionStep) {
        processExecutionRepository.findById(executionId).ifPresentOrElse(execution -> {
            ProcessExecutionStepEntity stepEntity = processExecutionStepMapper.toEntity(processExecutionStep);
            updateStep(execution, stepEntity);
            processExecutionRepository.save(execution);
        }, () -> LOGGER.warn("Execution {} not found in DB, ignoring step update", executionId));
    }

    @Transactional
    public void updateStepsStatuses(UUID executionId, List<ProcessExecutionStep> processExecutionSteps) {
        processExecutionRepository.findById(executionId).ifPresentOrElse(execution -> {
            processExecutionSteps.forEach(processExecutionStep -> {
                ProcessExecutionStepEntity stepEntity = processExecutionStepMapper.toEntity(processExecutionStep);
                updateStep(execution, stepEntity);
            });
            processExecutionRepository.save(execution);
        }, () -> LOGGER.warn("Execution {} not found in DB, ignoring steps update", executionId));
    }

    public UUID getReportId(UUID executionId) {
        return getExecutionEntity(executionId).getReportId();
    }

    public List<ResultInfos> getResultInfos(UUID executionId) {
        return Optional.ofNullable(getExecutionEntity(executionId).getSteps()).orElse(List.of()).stream()
            .filter(step -> step.getResultId() != null)
            .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
            .toList();
    }

    public String getDebugFileLocation(UUID executionId) {
        String debugFileLocation = getExecutionEntity(executionId).getDebugFileLocation();
        if (debugFileLocation == null) {
            throw new MonitorServerException(DEBUG_INFOS_NOT_FOUND, "Debug infos not found",
                Map.of("executionId", executionId));
        }
        return debugFileLocation;
    }

    public List<ProcessExecution> getLaunchedProcesses(ProcessType processType) {
        return processExecutionRepository.findByTypeAndStartedAtIsNotNullOrderByStartedAtDesc(processType.name()).stream()
            .map(processExecutionMapper::toDto)
            .toList();
    }

    public List<ProcessExecutionStep> getStepsInfos(UUID executionId) {
        return Optional.ofNullable(getExecutionEntity(executionId).getSteps()).orElse(List.of()).stream()
            .map(processExecutionStepMapper::toDto)
            .toList();
    }

    @Transactional
    public ProcessDeletionInfos deleteExecution(UUID executionId) {
        ProcessExecutionEntity entity = getExecutionEntity(executionId);
        ProcessDeletionInfos infos = ProcessDeletionInfos.fromProcessExecutionEntity(entity);

        processExecutionRepository.delete(entity);

        return infos;
    }

    private void updateStep(ProcessExecutionEntity execution, ProcessExecutionStepEntity stepEntity) {
        List<ProcessExecutionStepEntity> steps = Optional.ofNullable(execution.getSteps()).orElseGet(() -> {
            List<ProcessExecutionStepEntity> newSteps = new ArrayList<>();
            execution.setSteps(newSteps);
            return newSteps;
        });
        steps.stream()
            .filter(s -> s.getId().equals(stepEntity.getId()))
            .findFirst()
            .ifPresentOrElse(
                existingStep -> processExecutionStepMapper.updateEntityFromEntity(stepEntity, existingStep),
                () -> steps.add(stepEntity));
    }

    private ProcessExecutionEntity getExecutionEntity(UUID executionId) {
        return processExecutionRepository.findById(executionId)
            .orElseThrow(() -> new MonitorServerException(PROCESS_EXECUTION_NOT_FOUND, "Process execution not found",
                Map.of("executionId", executionId)));
    }
}
