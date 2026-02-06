/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.internal;

import org.gridsuite.monitor.commons.*;
import org.gridsuite.monitor.server.dto.ProcessExecution;
import org.gridsuite.monitor.server.dto.ReportLog;
import org.gridsuite.monitor.server.dto.ReportPage;
import org.gridsuite.monitor.server.dto.Severity;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.gridsuite.monitor.server.services.external.client.ReportRestClient;
import org.gridsuite.monitor.server.services.messaging.NotificationService;
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
@ExtendWith({MockitoExtension.class})
class MonitorServiceTest {

    @Mock
    private ProcessExecutionRepository executionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReportRestClient reportRestClient;

    @Mock
    private ResultService resultService;

    @InjectMocks
    private MonitorService monitorService;

    private SecurityAnalysisConfig securityAnalysisConfig;
    private UUID caseUuid;
    private UUID executionId;
    private String userId;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        executionId = UUID.randomUUID();
        userId = "user1";
        securityAnalysisConfig = new SecurityAnalysisConfig(
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

        UUID result = monitorService.executeProcess(caseUuid, userId, securityAnalysisConfig);

        assertThat(result).isNotNull();
        verify(executionRepository).save(argThat(execution ->
                        execution.getId() != null &&
                        ProcessType.SECURITY_ANALYSIS.name().equals(execution.getType()) &&
                        caseUuid.equals(execution.getCaseUuid()) &&
                        userId.equals(execution.getUserId()) &&
                        ProcessStatus.SCHEDULED.equals(execution.getStatus()) &&
                        execution.getScheduledAt() != null &&
                        execution.getStartedAt() == null
        ));
        verify(notificationService).sendProcessRunMessage(
                caseUuid,
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
                .userId(userId)
                .status(ProcessStatus.SCHEDULED)
                .scheduledAt(Instant.now())
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        monitorService.updateExecutionStatus(executionId, ProcessStatus.RUNNING, null, null, null);

        verify(executionRepository).findById(executionId);
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.RUNNING);
        assertThat(execution.getExecutionEnvName()).isNull();
        assertThat(execution.getStartedAt()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
        verify(executionRepository).save(execution);
    }

    @Test
    void updateExecutionStatusShouldUpdateAllFields() {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .userId(userId)
                .status(ProcessStatus.RUNNING)
                .scheduledAt(Instant.now())
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        String envName = "production-env";
        Instant startedAt = Instant.now().minusSeconds(60);
        Instant completedAt = Instant.now();

        monitorService.updateExecutionStatus(executionId, ProcessStatus.COMPLETED, envName, startedAt, completedAt);

        verify(executionRepository).findById(executionId);
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.COMPLETED);
        assertThat(execution.getExecutionEnvName()).isEqualTo(envName);
        assertThat(execution.getStartedAt()).isEqualTo(startedAt);
        assertThat(execution.getCompletedAt()).isEqualTo(completedAt);
        verify(executionRepository).save(execution);
    }

    @Test
    void updateExecutionStatusShouldHandleExecutionNotFound() {
        when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

        monitorService.updateExecutionStatus(executionId, ProcessStatus.COMPLETED, "env", Instant.now(), Instant.now());

        verify(executionRepository).findById(executionId);
        verify(executionRepository, never()).save(any());
    }

    @Test
    void updateStepStatusShouldAddNewStep() {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .id(executionId)
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .userId(userId)
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
                .userId(userId)
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
    void updateStepsStatusesShouldUpdateExistingSteps() {
        UUID stepId1 = UUID.randomUUID();
        UUID stepId2 = UUID.randomUUID();
        UUID originalResultId1 = UUID.randomUUID();
        UUID originalResultId2 = UUID.randomUUID();
        UUID newResultId1 = UUID.randomUUID();
        UUID newResultId2 = UUID.randomUUID();
        UUID newReportId1 = UUID.randomUUID();
        UUID newReportId2 = UUID.randomUUID();
        Instant startedAt1 = Instant.now().minusSeconds(60);
        Instant startedAt2 = Instant.now().minusSeconds(40);
        Instant completedAt1 = Instant.now();
        Instant completedAt2 = Instant.now();

        ProcessExecutionStepEntity existingStep1 = ProcessExecutionStepEntity.builder()
            .id(stepId1)
            .stepType("LOAD_NETWORK")
            .status(StepStatus.RUNNING)
            .resultId(originalResultId1)
            .startedAt(startedAt1)
            .build();
        ProcessExecutionStepEntity existingStep2 = ProcessExecutionStepEntity.builder()
            .id(stepId2)
            .stepType("LOAD_FLOW")
            .status(StepStatus.RUNNING)
            .resultId(originalResultId2)
            .startedAt(startedAt2)
            .build();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .id(executionId)
            .type(ProcessType.SECURITY_ANALYSIS.name())
            .caseUuid(caseUuid)
            .userId(userId)
            .status(ProcessStatus.RUNNING)
            .steps(new ArrayList<>(List.of(existingStep1, existingStep2)))
            .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        ProcessExecutionStep updateDto1 = ProcessExecutionStep.builder()
            .id(stepId1)
            .stepType("LOAD_NETWORK_UPDATED")
            .status(StepStatus.COMPLETED)
            .resultId(newResultId1)
            .resultType(ResultType.SECURITY_ANALYSIS)
            .reportId(newReportId1)
            .startedAt(startedAt1)
            .completedAt(completedAt1)
            .build();
        ProcessExecutionStep updateDto2 = ProcessExecutionStep.builder()
            .id(stepId2)
            .stepType("LOAD_FLOW_UPDATED")
            .status(StepStatus.COMPLETED)
            .resultId(newResultId2)
            .resultType(ResultType.SECURITY_ANALYSIS)
            .reportId(newReportId2)
            .startedAt(startedAt2)
            .completedAt(completedAt2)
            .build();
        monitorService.updateStepsStatuses(executionId, List.of(updateDto1, updateDto2));

        verify(executionRepository).findById(executionId);
        assertThat(execution.getSteps()).hasSize(2);
        ProcessExecutionStepEntity updatedStep1 = execution.getSteps().get(0);
        assertThat(updatedStep1.getId()).isEqualTo(stepId1);
        assertThat(updatedStep1.getStepType()).isEqualTo("LOAD_NETWORK_UPDATED");
        assertThat(updatedStep1.getStatus()).isEqualTo(StepStatus.COMPLETED);
        assertThat(updatedStep1.getResultId()).isEqualTo(newResultId1);
        assertThat(updatedStep1.getResultType()).isEqualTo(ResultType.SECURITY_ANALYSIS);
        assertThat(updatedStep1.getReportId()).isEqualTo(newReportId1);
        assertThat(updatedStep1.getCompletedAt()).isEqualTo(completedAt1);

        ProcessExecutionStepEntity updatedStep2 = execution.getSteps().get(1);
        assertThat(updatedStep2.getId()).isEqualTo(stepId2);
        assertThat(updatedStep2.getStepType()).isEqualTo("LOAD_FLOW_UPDATED");
        assertThat(updatedStep2.getStatus()).isEqualTo(StepStatus.COMPLETED);
        assertThat(updatedStep2.getResultId()).isEqualTo(newResultId2);
        assertThat(updatedStep2.getResultType()).isEqualTo(ResultType.SECURITY_ANALYSIS);
        assertThat(updatedStep2.getReportId()).isEqualTo(newReportId2);
        assertThat(updatedStep2.getCompletedAt()).isEqualTo(completedAt2);

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
                .userId(userId)
                .steps(List.of(step0, step1))
                .build();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        ReportPage reportPage1 = new ReportPage(1, List.of(
            new ReportLog("message1", Severity.INFO, 1, UUID.randomUUID()),
            new ReportLog("message2", Severity.WARN, 2, UUID.randomUUID())), 100, 10);
        ReportPage reportPage2 = new ReportPage(2, List.of(new ReportLog("message3", Severity.ERROR, 3, UUID.randomUUID())), 200, 20);

        when(reportRestClient.getReport(reportId1)).thenReturn(reportPage1);
        when(reportRestClient.getReport(reportId2)).thenReturn(reportPage2);

        List<ReportPage> result = monitorService.getReports(executionId);

        assertThat(result).hasSize(2).containsExactly(reportPage1, reportPage2);
        verify(executionRepository).findById(executionId);
        verify(reportRestClient).getReport(reportId1);
        verify(reportRestClient).getReport(reportId2);
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
                .userId(userId)
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

    @Test
    void getLaunchedProcesses() {
        UUID execution1Uuid = UUID.randomUUID();
        UUID case1Uuid = UUID.randomUUID();
        Instant scheduledAt1 = Instant.now().minusSeconds(60);
        Instant startedAt1 = Instant.now().minusSeconds(30);
        Instant completedAt1 = Instant.now();
        ProcessExecutionEntity execution1 = ProcessExecutionEntity.builder()
            .id(execution1Uuid)
            .type(ProcessType.SECURITY_ANALYSIS.name())
            .caseUuid(case1Uuid)
            .status(ProcessStatus.COMPLETED)
            .executionEnvName("env1")
            .scheduledAt(scheduledAt1)
            .startedAt(startedAt1)
            .completedAt(completedAt1)
            .userId("user1")
            .build();

        UUID execution2Uuid = UUID.randomUUID();
        UUID case2Uuid = UUID.randomUUID();
        Instant scheduledAt2 = Instant.now().minusSeconds(90);
        Instant startedAt2 = Instant.now().minusSeconds(80);
        ProcessExecutionEntity execution2 = ProcessExecutionEntity.builder()
            .id(execution2Uuid)
            .type(ProcessType.SECURITY_ANALYSIS.name())
            .caseUuid(case2Uuid)
            .status(ProcessStatus.RUNNING)
            .executionEnvName("env2")
            .scheduledAt(scheduledAt2)
            .startedAt(startedAt2)
            .userId("user2")
            .build();

        when(executionRepository.findByTypeAndStartedAtIsNotNullOrderByStartedAtDesc(ProcessType.SECURITY_ANALYSIS.name())).thenReturn(List.of(execution2, execution1));

        List<ProcessExecution> result = monitorService.getLaunchedProcesses(ProcessType.SECURITY_ANALYSIS);

        ProcessExecution processExecution1 = new ProcessExecution(execution1Uuid, ProcessType.SECURITY_ANALYSIS.name(), case1Uuid, ProcessStatus.COMPLETED, "env1", scheduledAt1, startedAt1, completedAt1, "user1");
        ProcessExecution processExecution2 = new ProcessExecution(execution2Uuid, ProcessType.SECURITY_ANALYSIS.name(), case2Uuid, ProcessStatus.RUNNING, "env2", scheduledAt2, startedAt2, null, "user2");

        assertThat(result).hasSize(2).containsExactly(processExecution2, processExecution1);
        verify(executionRepository).findByTypeAndStartedAtIsNotNullOrderByStartedAtDesc(ProcessType.SECURITY_ANALYSIS.name());
    }

    @Test
    void getStepsInfos() {
        UUID executionUuid = UUID.randomUUID();
        UUID stepId1 = UUID.randomUUID();
        UUID stepId2 = UUID.randomUUID();
        Instant startedAt1 = Instant.now();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .id(executionUuid)
            .type(ProcessType.SECURITY_ANALYSIS.name())
            .steps(List.of(ProcessExecutionStepEntity.builder().id(stepId1).stepType("loadNetwork").stepOrder(0).status(StepStatus.RUNNING).startedAt(startedAt1).build(),
                ProcessExecutionStepEntity.builder().id(stepId2).stepType("applyModifs").stepOrder(1).status(StepStatus.SCHEDULED).build()))
            .build();

        when(executionRepository.findById(executionUuid)).thenReturn(Optional.of(execution));

        Optional<List<ProcessExecutionStep>> result = monitorService.getStepsInfos(executionUuid);

        ProcessExecutionStep processExecutionStep1 = new ProcessExecutionStep(stepId1, "loadNetwork", 0, StepStatus.RUNNING, null, null, null, startedAt1, null);
        ProcessExecutionStep processExecutionStep2 = new ProcessExecutionStep(stepId2, "applyModifs", 1, StepStatus.SCHEDULED, null, null, null, null, null);

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2).containsExactly(processExecutionStep1, processExecutionStep2);
        verify(executionRepository).findById(executionUuid);
    }

    @Test
    void getStepsInfosShouldReturnEmptyWhenExecutionNotFound() {
        UUID executionUuid = UUID.randomUUID();
        when(executionRepository.findById(executionUuid)).thenReturn(Optional.empty());

        Optional<List<ProcessExecutionStep>> result = monitorService.getStepsInfos(executionUuid);

        assertThat(result).isEmpty();
        verify(executionRepository).findById(executionUuid);
    }

    @Test
    void getStepsInfosShouldReturnEmptyListWhenNoSteps() {
        UUID executionUuid = UUID.randomUUID();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .id(executionUuid)
            .type(ProcessType.SECURITY_ANALYSIS.name())
            .steps(null)
            .build();
        when(executionRepository.findById(executionUuid)).thenReturn(Optional.of(execution));

        Optional<List<ProcessExecutionStep>> result = monitorService.getStepsInfos(executionUuid);

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void deleteExecutionShouldDeleteResultsAndReports() {
        UUID reportId1 = UUID.randomUUID();
        UUID resultId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();
        UUID resultId2 = UUID.randomUUID();
        ProcessExecutionStepEntity step0 = ProcessExecutionStepEntity.builder()
            .id(UUID.randomUUID())
            .stepOrder(0)
            .reportId(reportId1)
            .resultId(resultId1)
            .build();
        ProcessExecutionStepEntity step1 = ProcessExecutionStepEntity.builder()
            .id(UUID.randomUUID())
            .stepOrder(1)
            .reportId(reportId2)
            .resultId(resultId2)
            .resultType(ResultType.SECURITY_ANALYSIS)
            .build();
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
            .id(executionId)
            .steps(List.of(step0, step1))
            .build();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        doNothing().when(executionRepository).deleteById(executionId);

        doNothing().when(reportRestClient).deleteReport(reportId1);
        doNothing().when(reportRestClient).deleteReport(reportId2);
        doNothing().when(resultService).deleteResult(any(ResultInfos.class));

        boolean done = monitorService.deleteExecution(executionId);
        assertThat(done).isTrue();

        verify(executionRepository).findById(executionId);
        verify(executionRepository).deleteById(executionId);
        verify(reportRestClient).deleteReport(reportId1);
        verify(reportRestClient).deleteReport(reportId2);
        verify(resultService, times(1)).deleteResult(any(ResultInfos.class));
    }

    @Test
    void deleteExecutionShouldReturnFalseWhenExecutionNotFound() {
        when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

        boolean done = monitorService.deleteExecution(executionId);
        assertThat(done).isFalse();

        verify(executionRepository).findById(executionId);
        verifyNoInteractions(reportRestClient);
        verifyNoInteractions(resultService);
        verify(executionRepository, never()).deleteById(executionId);
    }
}
