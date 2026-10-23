/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processconfig.ModificationInfo;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.clients.ReportRestClient;
import org.gridsuite.monitor.server.clients.S3RestClient;
import org.gridsuite.monitor.server.clients.UserIdentityRestClient;
import org.gridsuite.monitor.server.dto.processexecution.ProcessExecution;
import org.gridsuite.monitor.server.dto.report.MatchPosition;
import org.gridsuite.monitor.server.dto.report.Report;
import org.gridsuite.monitor.server.dto.report.ReportLog;
import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.gridsuite.monitor.server.dto.report.Severity;
import org.gridsuite.monitor.server.dto.useridentity.UserIdentities;
import org.gridsuite.monitor.server.dto.useridentity.UserIdentity;
import org.gridsuite.monitor.server.messaging.NotificationService;
import org.gridsuite.monitor.server.services.result.ResultQueryParams;
import org.gridsuite.monitor.server.services.result.ResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@ExtendWith({MockitoExtension.class})
class ProcessExecutionServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReportRestClient reportRestClient;

    @Mock
    private ResultService resultService;

    @Mock
    private S3RestClient s3RestClient;

    @Mock
    private UserIdentityRestClient userIdentityRestClient;

    @Mock
    private ProcessExecutionTxService processExecutionTxService;

    @InjectMocks
    private ProcessExecutionService processExecutionService;

    private SecurityAnalysisConfig securityAnalysisConfig;
    private UUID caseUuid;
    private UUID executionId;
    private UUID reportId;
    private String debugFileLocation;
    private String userId;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        executionId = UUID.randomUUID();
        reportId = UUID.randomUUID();
        debugFileLocation = "debug/file/location";
        userId = "user1";
        securityAnalysisConfig = new SecurityAnalysisConfig(
            UUID.randomUUID(),
            List.of(new ModificationInfo(UUID.randomUUID(), "descr1", true)),
            UUID.randomUUID()
        );
    }

    @Test
    void executeProcessCreateExecutionAndSendNotifications() {
        doReturn(Optional.of(new ProcessCreationResult(debugFileLocation, securityAnalysisConfig)))
            .when(processExecutionTxService).createExecution(eq(caseUuid), eq(userId), any(UUID.class), any(UUID.class), any(UUID.class), anyBoolean());

        Optional<UUID> result = processExecutionService.executeProcess(caseUuid, userId, UUID.randomUUID(), true);

        assertThat(result).isNotEmpty();
        verify(notificationService).sendProcessRunMessage(
            eq(caseUuid),
            eq(securityAnalysisConfig),
            eq(result.get()),
            any(UUID.class),
            eq(debugFileLocation)
        );
        verify(notificationService).sendProcessUpdatedMessage(any(UUID.class), any(ProcessType.class));
    }

    @Test
    void updateExecutionStatusShouldDelegateToTxService() {
        Instant startedAt = Instant.now().minusSeconds(60);
        Instant completedAt = Instant.now();
        String executionEnvName = "test-env";

        ProcessExecutionStatusUpdate update = new ProcessExecutionStatusUpdate(ProcessStatus.COMPLETED, executionEnvName, startedAt, completedAt);
        processExecutionService.updateExecutionStatus(executionId, ProcessType.SECURITY_ANALYSIS, update);

        verify(processExecutionTxService).updateExecutionStatus(executionId, update);
        verify(notificationService).sendProcessUpdatedMessage(executionId, ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void updateExecutionStatusOfCanceled() {
        Instant startedAt = Instant.now().minusSeconds(60);
        Instant completedAt = Instant.now();
        String executionEnvName = "test-env";

        ProcessExecutionStatusUpdate update = new ProcessExecutionStatusUpdate(ProcessStatus.CANCELED, executionEnvName, startedAt, completedAt);
        processExecutionService.updateExecutionStatus(executionId, ProcessType.SECURITY_ANALYSIS, update);

        verify(processExecutionTxService).updateExecutionStatus(executionId, update);
        verify(notificationService).sendProcessUpdatedMessage(executionId, ProcessType.SECURITY_ANALYSIS);
        verify(processExecutionTxService).deleteExecutionSteps(executionId);
        verify(processExecutionTxService).clearResourceReferences(executionId);
    }

    @Test
    void deleteAfterCancelShouldNotCleanResourcesAgain() {
        // Simulate cancel: resources are cleaned and references nulled
        ProcessExecutionStatusUpdate cancelUpdate = new ProcessExecutionStatusUpdate(ProcessStatus.CANCELED, null, null, null);
        processExecutionService.updateExecutionStatus(executionId, ProcessType.SECURITY_ANALYSIS, cancelUpdate);
        verify(processExecutionTxService).clearResourceReferences(executionId);

        // Simulate delete: deleteExecution returns infos with null reportId and null debugFileLocation (already cleared)
        ProcessDeletionInfos emptyInfos = new ProcessDeletionInfos(null, List.of(), null);
        when(processExecutionTxService.deleteExecution(executionId)).thenReturn(Optional.of(emptyInfos));
        processExecutionService.deleteExecution(executionId);

        // No external cleanup should happen since references were nulled
        verifyNoInteractions(reportRestClient);
        verifyNoInteractions(s3RestClient);
    }

    @Test
    void updateStepStatusShouldDelegateToTxService() {
        ProcessExecutionStep step = ProcessExecutionStep.builder()
            .id(UUID.randomUUID())
            .stepType("LOAD_FLOW")
            .stepOrder(1)
            .status(StepStatus.RUNNING)
            .resultId(UUID.randomUUID())
            .resultType(ResultType.SECURITY_ANALYSIS)
            .startedAt(Instant.now())
            .build();

        processExecutionService.updateStepStatus(executionId, ProcessType.SECURITY_ANALYSIS, step);

        verify(processExecutionTxService).updateStepStatus(executionId, step);
        verify(notificationService).sendProcessUpdatedMessage(executionId, ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void updateStepsStatusesShouldDelegateToTxService() {
        List<ProcessExecutionStep> steps = List.of(
            ProcessExecutionStep.builder()
                .id(UUID.randomUUID())
                .stepType("LOAD_FLOW")
                .stepOrder(1)
                .status(StepStatus.RUNNING)
                .build(),
            ProcessExecutionStep.builder()
                .id(UUID.randomUUID())
                .stepType("SECURITY_ANALYSIS")
                .stepOrder(2)
                .status(StepStatus.SCHEDULED)
                .build()
        );

        processExecutionService.updateStepsStatuses(executionId, ProcessType.SECURITY_ANALYSIS, steps);

        verify(processExecutionTxService).updateStepsStatuses(executionId, steps);
        verify(notificationService).sendProcessUpdatedMessage(executionId, ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void getLogsShouldDelegateToReportClient() {
        ReportPage reportPage = new ReportPage(1, List.of(new ReportLog("message", Severity.INFO, 1, UUID.randomUUID())), 1, 1);
        Set<String> severities = Set.of("INFO");
        when(reportRestClient.getLogs(reportId, "filter", severities, 2, 10)).thenReturn(reportPage);

        ReportPage result = processExecutionService.getLogs(reportId, "filter", severities, 2, 10);
        assertThat(result).isEqualTo(reportPage);

        verify(reportRestClient).getLogs(reportId, "filter", severities, 2, 10);
    }

    @Test
    void getLogsSearchShouldDelegateToReportClient() {
        Set<String> severities = Set.of("WARN");
        List<MatchPosition> matches = List.of(new MatchPosition(1, 2));
        when(reportRestClient.getLogsSearch(reportId, "filter", severities, "term", 20)).thenReturn(matches);

        List<MatchPosition> result = processExecutionService.getLogsSearch(reportId, "filter", severities, "term", 20);

        assertThat(result).isEqualTo(matches);
        verify(reportRestClient).getLogsSearch(reportId, "filter", severities, "term", 20);
    }

    @Test
    void getReportsShouldReturnReport() {
        Report report = new Report(UUID.randomUUID(), null, "root", Severity.INFO, 0, List.of());
        when(processExecutionTxService.getReportId(executionId)).thenReturn(Optional.of(reportId));
        when(reportRestClient.getReport(reportId)).thenReturn(report);

        assertThat(processExecutionService.getReports(executionId)).contains(report);

        verify(processExecutionTxService).getReportId(executionId);
        verify(reportRestClient).getReport(reportId);
    }

    @Test
    void getReportsReturnsEmptyWhenExecutionHasNoReport() {
        when(processExecutionTxService.getReportId(executionId)).thenReturn(Optional.empty());

        assertThat(processExecutionService.getReports(executionId)).isEmpty();

        verify(processExecutionTxService).getReportId(executionId);
        verifyNoInteractions(reportRestClient);
    }

    @Test
    void getReportsSeveritiesShouldReturnSeverities() {
        Set<String> severities = Set.of("INFO", "ERROR");
        when(processExecutionTxService.getReportId(executionId)).thenReturn(Optional.of(reportId));
        when(reportRestClient.getReportSeverities(reportId)).thenReturn(severities);

        assertThat(processExecutionService.getReportsSeverities(executionId)).contains(severities);

        verify(processExecutionTxService).getReportId(executionId);
        verify(reportRestClient).getReportSeverities(reportId);
    }

    @Test
    void getReportsSeveritiesReturnsEmptyWhenExecutionHasNoReport() {
        when(processExecutionTxService.getReportId(executionId)).thenReturn(Optional.empty());

        assertThat(processExecutionService.getReportsSeverities(executionId)).isEmpty();

        verify(processExecutionTxService).getReportId(executionId);
        verifyNoInteractions(reportRestClient);
    }

    @Test
    void getResultsShouldReturnOnlyFirstResult() {
        UUID resultId1 = UUID.randomUUID();
        UUID resultId2 = UUID.randomUUID();
        String result1 = "{\"result\": \"data1\"}";
        List<ResultInfos> resultInfos = List.of(new ResultInfos(resultId1, ResultType.SECURITY_ANALYSIS), new ResultInfos(resultId2, ResultType.SECURITY_ANALYSIS));
        ResultQueryParams queryParams = new ResultQueryParams("NMK_CONTINGENCIES", PageRequest.of(0, 25, Sort.unsorted()), null);
        when(resultService.getResult(resultInfos.get(0), queryParams))
            .thenReturn(result1);
        when(processExecutionTxService.getResultInfos(executionId)).thenReturn(Optional.of(resultInfos));

        Optional<String> results = processExecutionService.getResults(executionId, queryParams);

        assertThat(results).contains(result1);
        verify(processExecutionTxService).getResultInfos(executionId);
        verify(resultService).getResult(resultInfos.getFirst(), queryParams);
        verifyNoMoreInteractions(resultService);
    }

    @Test
    void getResultsShouldReturnEmptyWhenExecutionHasNoResults() {
        when(processExecutionTxService.getResultInfos(executionId)).thenReturn(Optional.of(List.of()));
        ResultQueryParams queryParams = new ResultQueryParams("NMK_CONTINGENCIES", PageRequest.of(0, 25), null);

        assertThat(processExecutionService.getResults(executionId, queryParams)).isEmpty();
        verifyNoInteractions(resultService);
    }

    @Test
    void exportResultsShouldReturnResults() {
        UUID resultId = UUID.randomUUID();
        byte[] exportResult = "result".getBytes();
        List<ResultInfos> resultInfos = List.of(new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS));
        ResultQueryParams queryParams = new ResultQueryParams("NMK_CONTINGENCIES", PageRequest.of(0, 25, Sort.unsorted()), null);
        when(resultService.exportResult(resultInfos.getFirst(), queryParams, ""))
                .thenReturn(exportResult);
        when(processExecutionTxService.getResultInfos(executionId)).thenReturn(Optional.of(resultInfos));

        Optional<byte[]> results = processExecutionService.exportResults(executionId, queryParams, "");

        assertThat(results).isPresent();
        assertThat(results.get()).contains(exportResult);
        verify(processExecutionTxService).getResultInfos(executionId);
    }

    @Test
    void deleteExecutionShouldDeleteResultsAndReports() {
        UUID resultId1 = UUID.randomUUID();
        UUID resultId2 = UUID.randomUUID();
        when(processExecutionTxService.deleteExecution(executionId))
            .thenReturn(Optional.of(new ProcessDeletionInfos(reportId,
                    List.of(new ResultInfos(resultId1, ResultType.SECURITY_ANALYSIS), new ResultInfos(resultId2, ResultType.SECURITY_ANALYSIS)),
                    debugFileLocation)));

        doNothing().when(reportRestClient).deleteReport(reportId);
        doNothing().when(resultService).deleteResult(any(ResultInfos.class));

        Optional<UUID> deletedExecutionId = processExecutionService.deleteExecution(executionId);
        assertThat(deletedExecutionId).contains(executionId);

        verify(reportRestClient).deleteReport(reportId);
        verify(resultService, times(2)).deleteResult(any(ResultInfos.class));
    }

    @Test
    void deleteExecutionShouldReturnFalseWhenExecutionNotFound() {
        when(processExecutionTxService.deleteExecution(executionId)).thenReturn(Optional.empty());

        Optional<UUID> deletedExecution = processExecutionService.deleteExecution(executionId);
        assertThat(deletedExecution).isNotPresent();

        verify(processExecutionTxService).deleteExecution(executionId);
        verifyNoInteractions(reportRestClient);
        verifyNoInteractions(resultService);
    }

    @Test
    void getExistingDebugInfo() throws Exception {
        byte[] expectedBytes = "zip-content".getBytes();

        when(processExecutionTxService.getDebugFileLocation(executionId)).thenReturn(Optional.of(debugFileLocation));
        when(s3RestClient.downloadDirectoryAsZip(debugFileLocation)).thenReturn(expectedBytes);

        Optional<byte[]> result = processExecutionService.getDebugInfos(executionId);

        assertThat(result).isPresent();
        assertThat(expectedBytes).isEqualTo(result.get());

        verify(processExecutionTxService).getDebugFileLocation(executionId);
        verify(s3RestClient).downloadDirectoryAsZip("debug/file/location");
    }

    @Test
    void getNotExistingExecutionDebugInfo() {
        when(processExecutionTxService.getDebugFileLocation(executionId)).thenReturn(Optional.empty());

        Optional<byte[]> result = processExecutionService.getDebugInfos(executionId);

        assertThat(result).isEmpty();

        verify(processExecutionTxService).getDebugFileLocation(executionId);
        verifyNoInteractions(s3RestClient);
    }

    @Test
    void getExistingDebugInfoError() throws Exception {

        when(processExecutionTxService.getDebugFileLocation(executionId)).thenReturn(Optional.of(debugFileLocation));

        when(s3RestClient.downloadDirectoryAsZip(debugFileLocation)).thenThrow(new IOException("S3 error"));

        PowsyblException exception = assertThrows(
            PowsyblException.class,
            () -> processExecutionService.getDebugInfos(executionId)
        );

        assertThat(exception.getMessage()).contains("An error occurred while downloading debug files");

        verify(processExecutionTxService).getDebugFileLocation(executionId);
        verify(s3RestClient).downloadDirectoryAsZip(debugFileLocation);
    }

    @Test
    void getProcessExecutionsShouldDelegateToTxService() {
        ProcessExecution processExecution1 = ProcessExecution.builder()
                .id(UUID.randomUUID())
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .processConfigId(UUID.randomUUID())
                .status(ProcessStatus.RUNNING)
                .scheduledAt(Instant.now())
                .userId(userId)
                .build();
        ProcessExecution processExecution1WithUserIdentity = processExecution1.withUserIdentity("titi tutu");

        ProcessExecution processExecution2 = ProcessExecution.builder()
                .id(UUID.randomUUID())
                .type(ProcessType.LOADFLOW.name())
                .caseUuid(caseUuid)
                .processConfigId(UUID.randomUUID())
                .status(ProcessStatus.SCHEDULED)
                .scheduledAt(Instant.now())
                .userId(userId)
                .build();
        ProcessExecution processExecution2WithUserIdentity = processExecution2.withUserIdentity("titi tutu");

        List<ProcessExecution> executions = List.of(processExecution1, processExecution2);

        Map<String, UserIdentity> userIdentities = new HashMap<>();
        userIdentities.put(userId, new UserIdentity("titi", "tutu"));

        when(userIdentityRestClient.getUserIdentities(List.of(processExecution1.userId())))
            .thenReturn(new UserIdentities(userIdentities));

        when(processExecutionTxService.getProcessExecutions()).thenReturn(executions);

        List<ProcessExecution> result = processExecutionService.getProcessExecutions();

        assertThat(result).isEqualTo(List.of(processExecution1WithUserIdentity, processExecution2WithUserIdentity));
        verify(processExecutionTxService).getProcessExecutions();
    }

    @Test
    void getExecutionReturnsExecution() {
        ProcessExecution processExecution = mock(ProcessExecution.class);
        ProcessExecution processExecutionWithUserIdentity = mock(ProcessExecution.class);
        Map<String, UserIdentity> userIdentities = new HashMap<>();
        userIdentities.put(userId, new UserIdentity("titi", "tutu"));

        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.of(processExecution));
        when(processExecution.userId()).thenReturn(userId);
        when(userIdentityRestClient.getUserIdentities(List.of(processExecution.userId())))
            .thenReturn(new UserIdentities(userIdentities));
        when(processExecution.withUserIdentity("titi tutu")).thenReturn(processExecutionWithUserIdentity);

        Optional<ProcessExecution> result = processExecutionService.getExecution(executionId);

        assertThat(result).contains(processExecutionWithUserIdentity);
        verify(processExecutionTxService).getExecution(executionId);
        verify(userIdentityRestClient).getUserIdentities(List.of(processExecution.userId()));
    }

    @Test
    void getExecutionNotFoundReturnsEmpty() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.empty());

        Optional<ProcessExecution> result = processExecutionService.getExecution(executionId);

        assertThat(result).isEmpty();
        verify(processExecutionTxService).getExecution(executionId);
    }

    @Test
    void getStepsInfosShouldDelegateToTxService() {
        List<ProcessExecutionStep> steps = List.of(
            ProcessExecutionStep.builder()
                .id(UUID.randomUUID())
                .stepType("LOAD_FLOW")
                .stepOrder(1)
                .status(StepStatus.COMPLETED)
                .startedAt(Instant.now().minusSeconds(30))
                .completedAt(Instant.now())
                .build()
        );

        when(processExecutionTxService.getStepsInfos(executionId)).thenReturn(Optional.of(steps));

        Optional<List<ProcessExecutionStep>> result = processExecutionService.getStepsInfos(executionId);

        assertThat(result).contains(steps);
        verify(processExecutionTxService).getStepsInfos(executionId);
    }

    @Test
    void getExecutionWithoutDebugInfo() {
        when(processExecutionTxService.getDebugFileLocation(executionId)).thenReturn(Optional.empty());

        Optional<byte[]> result = processExecutionService.getDebugInfos(executionId);

        assertThat(result).isEmpty();

        verifyNoInteractions(s3RestClient);
    }

    // --- cancelProcess tests ---

    private ProcessExecution buildExecution(ProcessStatus status) {
        return ProcessExecution.builder()
                .id(executionId)
                .type(ProcessType.SECURITY_ANALYSIS.name())
                .caseUuid(caseUuid)
                .processConfigId(UUID.randomUUID())
                .status(status)
                .executionEnvName("test-env")
                .userId(userId)
                .build();
    }

    @Test
    void cancelProcessShouldReturn404WhenExecutionNotFound() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = processExecutionService.cancelProcess(executionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(notificationService);
        verify(processExecutionTxService, never()).updateExecutionStatus(any(UUID.class), any(ProcessStatus.class));
    }

    @Test
    void cancelProcessShouldCancelImmediatelyWhenScheduled() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.of(buildExecution(ProcessStatus.SCHEDULED)));
        when(processExecutionTxService.updateStatusIfEligible(eq(executionId), eq(ProcessStatus.CANCELED), anyList())).thenReturn(true);

        ResponseEntity<Void> response = processExecutionService.cancelProcess(executionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(processExecutionTxService).updateStatusIfEligible(eq(executionId), eq(ProcessStatus.CANCELED), anyList());
        verify(notificationService).sendProcessUpdatedMessage(executionId, ProcessType.SECURITY_ANALYSIS);
        verify(notificationService).sendProcessCancelMessage(executionId);
    }

    @Test
    void cancelProcessShouldSendCancelMessageWhenRunning() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.of(buildExecution(ProcessStatus.RUNNING)));
        when(processExecutionTxService.updateStatusIfEligible(eq(executionId), eq(ProcessStatus.CANCELING), anyList())).thenReturn(true);

        ResponseEntity<Void> response = processExecutionService.cancelProcess(executionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(processExecutionTxService).updateStatusIfEligible(eq(executionId), eq(ProcessStatus.CANCELING), anyList());
        verify(notificationService).sendProcessUpdatedMessage(executionId, ProcessType.SECURITY_ANALYSIS);
        verify(notificationService).sendProcessCancelMessage(executionId);
    }

    @Test
    void cancelProcessShouldReturn204WhenAlreadyCancelled() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.of(buildExecution(ProcessStatus.CANCELED)));

        ResponseEntity<Void> response = processExecutionService.cancelProcess(executionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(processExecutionTxService, never()).updateExecutionStatus(any(UUID.class), any(ProcessStatus.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancelProcessShouldReturn204WhenAlreadyCancelling() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.of(buildExecution(ProcessStatus.CANCELING)));

        ResponseEntity<Void> response = processExecutionService.cancelProcess(executionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(processExecutionTxService, never()).updateExecutionStatus(any(UUID.class), any(ProcessStatus.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancelProcessShouldReturn409WhenCompleted() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.of(buildExecution(ProcessStatus.COMPLETED)));

        ResponseEntity<Void> response = processExecutionService.cancelProcess(executionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(processExecutionTxService, never()).updateExecutionStatus(any(UUID.class), any(ProcessStatus.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancelProcessShouldReturn409WhenFailed() {
        when(processExecutionTxService.getExecution(executionId)).thenReturn(Optional.of(buildExecution(ProcessStatus.FAILED)));

        ResponseEntity<Void> response = processExecutionService.cancelProcess(executionId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(processExecutionTxService, never()).updateExecutionStatus(any(UUID.class), any(ProcessStatus.class));
        verifyNoInteractions(notificationService);
    }
}
