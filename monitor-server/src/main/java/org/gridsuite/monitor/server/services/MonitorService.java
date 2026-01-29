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
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.server.dto.Report;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.events.ExecutionDeletedEvent;
import org.gridsuite.monitor.server.events.ProcessExecutionEvent;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final ProcessExecutionRepository executionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID executeProcess(UUID caseUuid, ProcessConfig processConfig) {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .type(processConfig.processType().name())
            .caseUuid(caseUuid)
            .status(ProcessStatus.SCHEDULED)
            .scheduledAt(Instant.now())
            .build();
        executionRepository.save(execution);

        eventPublisher.publishEvent(new ProcessExecutionEvent(caseUuid, processConfig, execution.getId()));

        return execution.getId();
    }

    @Transactional
    public void updateExecutionStatus(UUID executionId, ProcessStatus status, String executionEnvName, Instant completedAt) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            execution.setStatus(status);
            if (executionEnvName != null) {
                execution.setExecutionEnvName(executionEnvName);
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
    public List<UUID> getReportIds(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(execution -> execution.getSteps().stream()
                .map(ProcessExecutionStepEntity::getReportId)
                .filter(Objects::nonNull)
                .toList())
            .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<ResultInfos> getResultInfos(UUID executionId) {
        return executionRepository.findById(executionId)
                .map(execution -> execution.getSteps().stream()
                .filter(step -> step.getResultId() != null)
                .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
                .toList())
            .orElse(List.of());
    }

    @Transactional
    public boolean deleteExecution(UUID executionId) {
        return executionRepository.findById(executionId)
            .map(entity -> {
                List<ResultInfos> resultInfos = entity.getSteps().stream()
                    .filter(s -> s.getResultId() != null && s.getResultType() != null)
                    .map(s -> new ResultInfos(s.getResultId(), s.getResultType()))
                    .toList();
                List<UUID> reportIds = entity.getSteps().stream()
                    .map(ProcessExecutionStepEntity::getReportId)
                    .filter(Objects::nonNull)
                    .toList();

                executionRepository.delete(entity);
                eventPublisher.publishEvent(new ExecutionDeletedEvent(resultInfos, reportIds));

                return true;
            })
            .orElse(false);
    }
}
