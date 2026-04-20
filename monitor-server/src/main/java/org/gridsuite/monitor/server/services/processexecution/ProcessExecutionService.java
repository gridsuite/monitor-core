/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.gridsuite.monitor.server.clients.ReportRestClient;
import org.gridsuite.monitor.server.dto.processexecution.ProcessExecution;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.mappers.processexecution.ProcessExecutionMapper;
import org.gridsuite.monitor.server.mappers.processexecution.ProcessExecutionStepMapper;
import org.gridsuite.monitor.server.messaging.NotificationService;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.gridsuite.monitor.server.clients.S3RestClient;
import org.gridsuite.monitor.server.services.processconfig.ProcessConfigService;
import org.gridsuite.monitor.server.services.result.ResultService;
import org.gridsuite.monitor.server.utils.S3PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ProcessExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutionService.class);

    private final ProcessExecutionRepository processExecutionRepository;
    private final NotificationService notificationService;
    private final ProcessConfigService processConfigService;
    private final ReportRestClient reportRestClient;
    private final ResultService resultService;
    private final S3RestClient s3RestClient;
    private final S3PathResolver s3PathResolver;

    private final ProcessExecutionStepMapper processExecutionStepMapper;
    private final ProcessExecutionMapper processExecutionMapper;

    public ProcessExecutionService(ProcessExecutionRepository processExecutionRepository,
                          NotificationService notificationService,
                          ProcessConfigService processConfigService,
                          ReportRestClient reportRestClient,
                          ResultService resultService,
                          S3RestClient s3RestClient,
                          S3PathResolver s3PathResolver,
                          ProcessExecutionStepMapper processExecutionStepMapper,
                          ProcessExecutionMapper processExecutionMapper) {
        this.processExecutionRepository = processExecutionRepository;
        this.notificationService = notificationService;
        this.processConfigService = processConfigService;
        this.reportRestClient = reportRestClient;
        this.resultService = resultService;
        this.s3RestClient = s3RestClient;
        this.s3PathResolver = s3PathResolver;
        this.processExecutionStepMapper = processExecutionStepMapper;
        this.processExecutionMapper = processExecutionMapper;
    }

    @Transactional
    public Optional<UUID> executeProcess(UUID caseUuid, String userId, UUID processConfigId, boolean isDebug) {
        UUID executionId = UUID.randomUUID();
        Optional<PersistedProcessConfig> persistedProcessConfig = processConfigService.getProcessConfig(processConfigId);
        if (persistedProcessConfig.isPresent()) {
            ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .type(persistedProcessConfig.get().processConfig().processType().name())
                .caseUuid(caseUuid)
                .processConfigId(persistedProcessConfig.get().id())
                .status(ProcessStatus.SCHEDULED)
                .scheduledAt(Instant.now())
                .userId(userId)
                .build();
            if (isDebug) {
                execution.setDebugFileLocation(s3PathResolver.toDebugLocation(persistedProcessConfig.get().processConfig().processType().name(), executionId));
            }
            processExecutionRepository.save(execution);

            notificationService.sendProcessRunMessage(caseUuid, persistedProcessConfig.get().processConfig(), execution.getId(), execution.getDebugFileLocation());
            notificationService.sendProcessUpdatedMessage(persistedProcessConfig.get().processConfig().processType(), execution.getId());

            return Optional.of(execution.getId());
        } else {
            return Optional.empty();
        }
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

    private void updateStep(ProcessExecutionEntity execution, ProcessExecutionStepEntity stepEntity) {
        List<ProcessExecutionStepEntity> steps = Optional.ofNullable(execution.getSteps()).orElseGet(() -> {
            List<ProcessExecutionStepEntity> newSteps = new java.util.ArrayList<>();
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

    @Transactional(readOnly = true)
    public Optional<List<ReportPage>> getReports(UUID executionId) {
        Optional<List<UUID>> reportIds = getReportIds(executionId);
        return reportIds.map(ids -> ids.stream()
                .map(reportRestClient::getReport)
                .toList());
    }

    private Optional<List<UUID>> getReportIds(UUID executionId) {
        return processExecutionRepository.findById(executionId)
            .map(execution -> Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
                .map(ProcessExecutionStepEntity::getReportId)
                .filter(java.util.Objects::nonNull)
                .toList());
    }

    @Transactional(readOnly = true)
    public Optional<List<String>> getResults(UUID executionId) {
        Optional<List<ResultInfos>> resultInfos = getResultInfos(executionId);
        return resultInfos.map(results -> results.stream()
                .map(resultService::getResult)
                .toList());
    }

    @Transactional(readOnly = true)
    public Optional<byte[]> getDebugInfos(UUID executionId) {
        return processExecutionRepository.findById(executionId)
            .map(ProcessExecutionEntity::getDebugFileLocation)
            .filter(Objects::nonNull)
            .map(debugFileLocation -> {
                try {
                    return s3RestClient.downloadDirectoryAsZip(debugFileLocation);
                } catch (IOException e) {
                    throw new PowsyblException("An error occurred while downloading debug files", e);
                }
            });
    }

    private Optional<List<ResultInfos>> getResultInfos(UUID executionId) {
        return processExecutionRepository.findById(executionId)
            .map(execution -> Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
                .filter(step -> step.getResultId() != null)
                .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
                .toList());
    }

    @Transactional(readOnly = true)
    public List<ProcessExecution> getLaunchedProcesses(ProcessType processType) {
        return processExecutionRepository.findByTypeAndStartedAtIsNotNullOrderByStartedAtDesc(processType.name()).stream()
            .map(processExecutionMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<List<ProcessExecutionStep>> getStepsInfos(UUID executionId) {
        Optional<ProcessExecutionEntity> entity = processExecutionRepository.findById(executionId);
        if (entity.isPresent()) {
            return entity.map(execution -> Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
                .map(processExecutionStepMapper::toDto)
                .toList());
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<UUID> deleteExecution(UUID executionId) {
        List<ResultInfos> resultIds = new ArrayList<>();
        List<UUID> reportIds = new ArrayList<>();

        Optional<ProcessExecutionEntity> executionEntity = processExecutionRepository.findById(executionId);
        if (executionEntity.isPresent()) {
            ProcessExecutionEntity entity = executionEntity.get();
            Optional.ofNullable(entity.getSteps()).orElse(List.of()).forEach(step -> {
                if (step.getResultId() != null && step.getResultType() != null) {
                    resultIds.add(new ResultInfos(step.getResultId(), step.getResultType()));
                }
                if (step.getReportId() != null) {
                    reportIds.add(step.getReportId());
                }
            });
            resultIds.forEach(resultService::deleteResult);
            reportIds.forEach(reportRestClient::deleteReport);

            processExecutionRepository.delete(entity);

            return Optional.of(executionId);
        }
        return Optional.empty();
    }
}
