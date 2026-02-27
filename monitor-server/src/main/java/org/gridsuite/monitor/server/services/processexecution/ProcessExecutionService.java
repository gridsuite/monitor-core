/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.monitor.commons.api.types.result.ResultInfos;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStep;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.clients.ReportRestClient;
import org.gridsuite.monitor.server.dto.processexecution.ProcessExecution;
import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.mappers.processexecution.ProcessExecutionMapper;
import org.gridsuite.monitor.server.mappers.processexecution.ProcessExecutionStepMapper;
import org.gridsuite.monitor.server.messaging.NotificationService;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.gridsuite.monitor.server.services.S3RestService;
import org.gridsuite.monitor.server.services.result.ResultService;
import org.gridsuite.monitor.server.utils.S3PathResolver;
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

    private final ProcessExecutionRepository executionRepository;
    private final NotificationService notificationService;
    private final ReportRestClient reportRestClient;
    private final ResultService resultService;
    private final S3RestService s3RestService;
    private final S3PathResolver s3PathResolver;

    public ProcessExecutionService(ProcessExecutionRepository executionRepository,
                                   NotificationService notificationService,
                                   ReportRestClient reportRestClient,
                                   ResultService resultService,
                                   S3RestService s3RestService,
                                   S3PathResolver s3PathResolver) {
        this.executionRepository = executionRepository;
        this.notificationService = notificationService;
        this.reportRestClient = reportRestClient;
        this.resultService = resultService;
        this.s3RestService = s3RestService;
        this.s3PathResolver = s3PathResolver;
    }

    @Transactional
    public UUID executeProcess(UUID caseUuid, String userId, ProcessConfig processConfig, boolean isDebug) {
        UUID executionId = UUID.randomUUID();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .id(executionId)
            .type(processConfig.processType().name())
            .caseUuid(caseUuid)
            .status(ProcessStatus.SCHEDULED)
            .scheduledAt(Instant.now())
            .userId(userId)
            .build();
        if (isDebug) {
            execution.setDebugFileLocation(s3PathResolver.toDebugLocation(processConfig.processType().name(), executionId));
        }
        executionRepository.save(execution);

        notificationService.sendProcessRunMessage(caseUuid, processConfig, execution.getId(), execution.getDebugFileLocation());

        return execution.getId();
    }

    @Transactional
    public void updateExecutionStatus(UUID executionId, ProcessStatus status, String executionEnvName, Instant startedAt, Instant completedAt) {
        executionRepository.findById(executionId).ifPresent(execution -> {
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
            executionRepository.save(execution);
        });
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
                existingStep -> {
                    existingStep.setStatus(stepEntity.getStatus());
                    existingStep.setStepType(stepEntity.getStepType());
                    existingStep.setStepOrder(stepEntity.getStepOrder());
                    existingStep.setStartedAt(stepEntity.getStartedAt());
                    existingStep.setCompletedAt(stepEntity.getCompletedAt());
                    existingStep.setResultId(stepEntity.getResultId());
                    existingStep.setResultType(stepEntity.getResultType());
                    existingStep.setReportId(stepEntity.getReportId());
                },
                () -> steps.add(stepEntity));
    }

    @Transactional
    public void updateStepStatus(UUID executionId, ProcessExecutionStep processExecutionStep) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            ProcessExecutionStepEntity stepEntity = toStepEntity(processExecutionStep);
            updateStep(execution, stepEntity);
            executionRepository.save(execution);
        });
    }

    @Transactional
    public void updateStepsStatuses(UUID executionId, List<ProcessExecutionStep> processExecutionSteps) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            processExecutionSteps.forEach(processExecutionStep -> {
                ProcessExecutionStepEntity stepEntity = toStepEntity(processExecutionStep);
                updateStep(execution, stepEntity);
            });
            executionRepository.save(execution);
        });
    }

    private ProcessExecutionStepEntity toStepEntity(ProcessExecutionStep processExecutionStep) {
        return ProcessExecutionStepEntity.builder()
                .id(processExecutionStep.getId())
                .stepType(processExecutionStep.getStepType())
                .stepOrder(processExecutionStep.getStepOrder())
                .status(processExecutionStep.getStatus())
                .resultId(processExecutionStep.getResultId())
                .resultType(processExecutionStep.getResultType())
                .reportId(processExecutionStep.getReportId())
                .startedAt(processExecutionStep.getStartedAt())
                .completedAt(processExecutionStep.getCompletedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReportPage> getReports(UUID executionId) {
        List<UUID> reportIds = getReportIds(executionId);
        return reportIds.stream()
                .map(reportRestClient::getReport)
                .toList();
    }

    private List<UUID> getReportIds(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(execution -> Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
                .map(ProcessExecutionStepEntity::getReportId)
                .filter(java.util.Objects::nonNull)
                .toList())
            .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<String> getResults(UUID executionId) {
        List<ResultInfos> resultInfos = getResultInfos(executionId);
        return resultInfos.stream()
                .map(resultService::getResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<byte[]> getDebugInfos(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(ProcessExecutionEntity::getDebugFileLocation)
            .filter(Objects::nonNull)
            .map(debugFileLocation -> {
                try {
                    return s3RestService.downloadDirectoryAsZip(debugFileLocation);
                } catch (IOException e) {
                    throw new PowsyblException("An error occurred while downloading debug files", e);
                }
            });
    }

    private List<ResultInfos> getResultInfos(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(execution -> Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
                .filter(step -> step.getResultId() != null)
                .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
                .toList())
            .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<ProcessExecution> getLaunchedProcesses(ProcessType processType) {
        return executionRepository.findByTypeAndStartedAtIsNotNullOrderByStartedAtDesc(processType.name()).stream()
            .map(ProcessExecutionMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<List<ProcessExecutionStep>> getStepsInfos(UUID executionId) {
        Optional<ProcessExecutionEntity> entity = executionRepository.findById(executionId);
        if (entity.isPresent()) {
            return entity.map(execution -> Optional.ofNullable(execution.getSteps()).orElse(List.of()).stream()
                .map(ProcessExecutionStepMapper::toDto)
                .toList());
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean deleteExecution(UUID executionId) {
        List<ResultInfos> resultIds = new ArrayList<>();
        List<UUID> reportIds = new ArrayList<>();

        Optional<ProcessExecutionEntity> executionEntity = executionRepository.findById(executionId);
        if (executionEntity.isPresent()) {
            Optional.ofNullable(executionEntity.get().getSteps()).orElse(List.of()).forEach(step -> {
                if (step.getResultId() != null && step.getResultType() != null) {
                    resultIds.add(new ResultInfos(step.getResultId(), step.getResultType()));
                }
                if (step.getReportId() != null) {
                    reportIds.add(step.getReportId());
                }
            });
            resultIds.forEach(resultService::deleteResult);
            reportIds.forEach(reportRestClient::deleteReport);

            executionRepository.deleteById(executionId);

            return true;
        }
        return false;
    }
}
