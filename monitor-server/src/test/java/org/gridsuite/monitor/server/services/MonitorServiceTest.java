/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.*;
import org.gridsuite.monitor.server.dto.Report;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    @Mock
    private ProcessExecutionRepository executionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReportService reportService;

    @Mock
    private ResultService resultService;

    @InjectMocks
    private MonitorService monitorService;

    private SecurityAnalysisConfig securityAnalysisConfig;
    private UUID caseUuid;
    private UUID executionId;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        executionId = UUID.randomUUID();
        securityAnalysisConfig = new SecurityAnalysisConfig(
                caseUuid,
                null,
                UUID.randomUUID(),
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID())
        );
    }

    @Test
    void executeProcessCreateExecutionAndSendNotification() {
        UUID expectedExecutionId = UUID.randomUUID();
        when(executionRepository.save(any(ProcessExecutionEntity.class)))
                .thenAnswer(invocation -> {
                    ProcessExecutionEntity entity = invocation.getArgument(0);
                    entity.setId(expectedExecutionId); // mock id generation
                    return entity;
                });

        UUID result = monitorService.executeProcess(securityAnalysisConfig);

        assertThat(result).isNotNull();
        verify(executionRepository).save(argThat(execution ->
                        execution.getId() != null &&
                        ProcessType.SECURITY_ANALYSIS.name().equals(execution.getType()) &&
                        caseUuid.equals(execution.getCaseUuid()) &&
                        ProcessStatus.SCHEDULED.equals(execution.getStatus()) &&
                        execution.getScheduledAt() != null
        ));
        verify(notificationService).sendProcessRunMessage(
                securityAnalysisConfig,
                result
        );
    }

    @Test
    void updateExecutionStatusShouldUpdateStatusOnly() {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .status(ProcessStatus.SCHEDULED)
                .scheduledAt(Instant.now())
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        monitorService.updateExecutionStatus(executionId, ProcessStatus.RUNNING, null, null);

        verify(executionRepository).findById(executionId);
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.RUNNING);
        assertThat(execution.getExecutionEnvName()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
        verify(executionRepository).save(execution);
    }

    @Test
    void updateExecutionStatusShouldUpdateAllFields() {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .status(ProcessStatus.RUNNING)
                .scheduledAt(Instant.now())
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        String envName = "production-env";
        Instant completedAt = Instant.now();

        monitorService.updateExecutionStatus(executionId, ProcessStatus.COMPLETED, envName, completedAt);

        verify(executionRepository).findById(executionId);
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.COMPLETED);
        assertThat(execution.getExecutionEnvName()).isEqualTo(envName);
        assertThat(execution.getCompletedAt()).isEqualTo(completedAt);
        verify(executionRepository).save(execution);
    }

    @Test
    void updateExecutionStatusShouldHandleExecutionNotFound() {
        when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

        monitorService.updateExecutionStatus(executionId, ProcessStatus.COMPLETED, "env", Instant.now());

        verify(executionRepository).findById(executionId);
        verify(executionRepository, never()).save(any());
    }

    @Test
    void updateStepStatusShouldAddNewStep() {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .status(ProcessStatus.RUNNING)
                .steps(new ArrayList<>())
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        UUID stepId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Instant startedAt = Instant.now();
        ProcessExecutionStep processExecutionStep = ProcessExecutionStep.builder()
                .id(stepId)
                .stepType("LOAD_FLOW")
                .status(StepStatus.RUNNING)
                .resultId(resultId)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .reportId(reportId)
                .startedAt(startedAt)
                .build();

        monitorService.updateStepStatus(executionId, processExecutionStep);

        verify(executionRepository).findById(executionId);
        assertThat(execution.getSteps()).hasSize(1);
        ProcessExecutionStepEntity addedStep = execution.getSteps().getFirst();
        assertThat(addedStep.getId()).isEqualTo(stepId);
        assertThat(addedStep.getStepType()).isEqualTo("LOAD_FLOW");
        assertThat(addedStep.getStatus()).isEqualTo(StepStatus.RUNNING);
        assertThat(addedStep.getResultId()).isEqualTo(resultId);
        assertThat(addedStep.getResultType()).isEqualTo(ResultType.SECURITY_ANALYSIS);
        assertThat(addedStep.getReportId()).isEqualTo(reportId);
        assertThat(addedStep.getStartedAt()).isEqualTo(startedAt);
        verify(executionRepository).save(execution);
    }

    @Test
    void updateStepStatusShouldUpdateExistingStep() {
        UUID stepId = UUID.randomUUID();
        UUID originalResultId = UUID.randomUUID();
        UUID newResultId = UUID.randomUUID();
        UUID newReportId = UUID.randomUUID();
        Instant startedAt = Instant.now().minusSeconds(60);
        Instant completedAt = Instant.now();
        ProcessExecutionStepEntity existingStep = ProcessExecutionStepEntity.builder()
                .id(stepId)
                .stepType("LOAD_FLOW")
                .status(StepStatus.RUNNING)
                .resultId(originalResultId)
                .startedAt(startedAt)
                .build();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .status(ProcessStatus.RUNNING)
                .steps(new ArrayList<>(List.of(existingStep)))
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        ProcessExecutionStep updateDto = ProcessExecutionStep.builder()
                .id(stepId)
                .stepType("LOAD_FLOW_UPDATED")
                .status(StepStatus.COMPLETED)
                .resultId(newResultId)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .reportId(newReportId)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();

        monitorService.updateStepStatus(executionId, updateDto);

        verify(executionRepository).findById(executionId);
        assertThat(execution.getSteps()).hasSize(1);
        ProcessExecutionStepEntity updatedStep = execution.getSteps().getFirst();
        assertThat(updatedStep.getId()).isEqualTo(stepId);
        assertThat(updatedStep.getStepType()).isEqualTo("LOAD_FLOW_UPDATED");
        assertThat(updatedStep.getStatus()).isEqualTo(StepStatus.COMPLETED);
        assertThat(updatedStep.getResultId()).isEqualTo(newResultId);
        assertThat(updatedStep.getResultType()).isEqualTo(ResultType.SECURITY_ANALYSIS);
        assertThat(updatedStep.getReportId()).isEqualTo(newReportId);
        assertThat(updatedStep.getCompletedAt()).isEqualTo(completedAt);
        verify(executionRepository).save(execution);
    }

    @Test
    void getReportsShouldReturnReports() {
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();
        ProcessExecutionStepEntity step0 = ProcessExecutionStepEntity.builder()
                .id(UUID.randomUUID())
                .stepOrder(0)
                .reportId(reportId1)
                .build();
        ProcessExecutionStepEntity step1 = ProcessExecutionStepEntity.builder()
                .id(UUID.randomUUID())
                .stepOrder(1)
                .reportId(reportId2)
                .build();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .steps(List.of(step0, step1))
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        Report report1 = new Report(reportId1, null, "Report 1", null, List.of());
        Report report2 = new Report(reportId2, null, "Report 2", null, List.of());
        when(reportService.getReport(reportId1)).thenReturn(report1);
        when(reportService.getReport(reportId2)).thenReturn(report2);

        List<Report> result = monitorService.getReports(executionId);

        assertThat(result).hasSize(2).containsExactly(report1, report2);
        verify(executionRepository).findById(executionId);
        verify(reportService).getReport(reportId1);
        verify(reportService).getReport(reportId2);
    }

    @Test
    void getResultsShouldReturnResults() {
        UUID resultId1 = UUID.randomUUID();
        UUID resultId2 = UUID.randomUUID();
        ProcessExecutionStepEntity step0 = ProcessExecutionStepEntity.builder()
                .id(UUID.randomUUID())
                .stepOrder(0)
                .resultId(resultId1)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .build();
        ProcessExecutionStepEntity step1 = ProcessExecutionStepEntity.builder()
                .id(UUID.randomUUID())
                .stepOrder(1)
                .resultId(resultId2)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .build();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .steps(List.of(step0, step1))
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        String result1 = "{\"result\": \"data1\"}";
        String result2 = "{\"result\": \"data2\"}";
        when(resultService.getResult(new ResultInfos(resultId1, ResultType.SECURITY_ANALYSIS)))
                .thenReturn(result1);
        when(resultService.getResult(new ResultInfos(resultId2, ResultType.SECURITY_ANALYSIS)))
                .thenReturn(result2);

        List<String> results = monitorService.getResults(executionId);

        assertThat(results).hasSize(2).containsExactly(result1, result2);
        verify(executionRepository).findById(executionId);
        verify(resultService, times(2)).getResult(any(ResultInfos.class));
    }

}
