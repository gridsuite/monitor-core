/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.orchestrator.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.process.commons.ProcessExecutionStep;
import org.gridsuite.process.commons.ProcessStatus;
import org.gridsuite.process.commons.ResultInfos;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.orchestrator.server.dto.Report;
import org.gridsuite.process.orchestrator.server.entities.*;
import org.gridsuite.process.orchestrator.server.repositories.ProcessExecutionRepository;
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
public class ProcessOrchestratorService {

    private final ProcessExecutionRepository executionRepository;
    private final NotificationService notificationService;
    private final DummyReportService reportService;
    private final ResultService resultService;

    @Transactional
    public UUID executeProcess(ProcessConfig processConfig) {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .type(processConfig.processType().name())
            .caseUuid(processConfig.caseUuid())
            .status(ProcessStatus.SCHEDULED)
            .scheduledAt(Instant.now())
            .build();
        executionRepository.save(execution);

        notificationService.sendProcessRunMessage(processConfig, execution.getId());

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
                        existingStep.setPreviousStepId(stepEntity.getPreviousStepId());
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
                .previousStepId(processExecutionStep.getPreviousStepId())
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
            //FIXME: Sort steps by order
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
                .map(info -> resultService.getResult(info))
                .toList();
    }

    private List<ResultInfos> getResultInfos(UUID executionId) {
        return executionRepository.findById(executionId)
                //FIXME: Sort steps by order
                .map(execution -> execution.getSteps().stream()
                .filter(step -> step.getResultId() != null)
                .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
                .toList())
            .orElse(List.of());
    }
}
