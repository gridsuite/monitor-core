/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.server.dto.ProcessExecution;
import org.gridsuite.monitor.server.dto.Report;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.mapper.ProcessExecutionMapper;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final ProcessExecutionRepository executionRepository;
    private final NotificationService notificationService;
    private final ReportService reportService;
    private final ResultService resultService;
    private final ProcessExecutionMapper processExecutionMapper;

    @Transactional
    public UUID executeProcess(UUID caseUuid, String userId, ProcessConfig processConfig) {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .type(processConfig.processType().name())
            .caseUuid(caseUuid)
            .status(ProcessStatus.SCHEDULED)
            .scheduledAt(Instant.now())
            .userId(userId)
            .build();
        executionRepository.save(execution);

        notificationService.sendProcessRunMessage(caseUuid, processConfig, execution.getId());

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

    @Transactional
    public void updateStepStatus(UUID executionId, ProcessExecutionStep processExecutionStep) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            ProcessExecutionStepEntity stepEntity = toStepEntity(processExecutionStep);
            execution.getSteps().stream()
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
                    () -> execution.getSteps().add(stepEntity));
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
    public List<Report> getReports(UUID executionId) {
        List<UUID> reportIds = getReportIds(executionId);
        return reportIds.stream()
                .map(reportService::getReport)
                .toList();
    }

    private List<UUID> getReportIds(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(execution -> execution.getSteps().stream()
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

    private List<ResultInfos> getResultInfos(UUID executionId) {
        return executionRepository.findById(executionId)
                .map(execution -> execution.getSteps().stream()
                .filter(step -> step.getResultId() != null)
                .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
                .toList())
            .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<ProcessExecution> getLaunchedProcesses(ProcessType processType) {
        return executionRepository.findByTypeAndStartedAtIsNotNullOrderByStartedAtDesc(processType.name()).stream()
            .map(processExecutionMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessExecutionStep> getStepsInfos(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(execution -> execution.getSteps().stream()
                .map(step ->
                    new ProcessExecutionStep(step.getId(), step.getStepType(), step.getStepOrder(),
                        step.getStatus(), step.getResultId(), step.getResultType(),
                        step.getReportId(), step.getStartedAt(), step.getCompletedAt()))
                .toList())
            .orElse(List.of());
    }
}
