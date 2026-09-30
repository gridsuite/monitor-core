/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processconfig.ModificationInfo;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.server.PropertyServerNameProvider;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.dto.processexecution.ProcessExecution;
import org.gridsuite.monitor.server.dto.report.MatchPosition;
import org.gridsuite.monitor.server.dto.report.Report;
import org.gridsuite.monitor.server.dto.report.ReportLog;
import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.gridsuite.monitor.server.dto.report.Severity;
import org.gridsuite.monitor.server.services.processconfig.ProcessConfigService;
import org.gridsuite.monitor.server.services.processexecution.ProcessExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@WebMvcTest(controllers = {MonitorController.class, PropertyServerNameProvider.class})
class MonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessExecutionService processExecutionService;

    @MockitoBean
    private ProcessConfigService processConfigService;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @NullSource
    void executeProcessShouldReturnExecutionId(Boolean isDebug) throws Exception {
        UUID caseUuid = UUID.randomUUID();
        UUID parametersUuid = UUID.randomUUID();
        UUID modificationUuid = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        UUID processConfigUuid = UUID.randomUUID();

        SecurityAnalysisConfig config = new SecurityAnalysisConfig(
                parametersUuid,
                List.of(new ModificationInfo(modificationUuid, "descr", true)),
                loadflowParametersUuid
        );
        PersistedProcessConfig persistedProcessConfig = new PersistedProcessConfig(processConfigUuid, config);

        boolean expectedDebugValue = Boolean.TRUE.equals(isDebug);

        when(processConfigService.getProcessConfig(processConfigUuid)).thenReturn(Optional.of(persistedProcessConfig));
        when(processExecutionService.executeProcess(any(UUID.class), any(String.class), any(UUID.class), eq(expectedDebugValue)))
                .thenReturn(Optional.of(executionId));

        MockHttpServletRequestBuilder request = post("/v1/execute")
            .param("caseUuid", caseUuid.toString())
            .param("processConfigUuid", processConfigUuid.toString())
            .header("userId", "user1");

        if (isDebug != null) {
            request.param("isDebug", isDebug.toString());
        }

        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(executionId.toString()));

        verify(processExecutionService).executeProcess(eq(caseUuid), any(String.class), any(UUID.class), eq(expectedDebugValue));
    }

    @Test
    void executeProcessWithConfigNotFoundShouldReturnError() throws Exception {
        UUID caseUuid = UUID.randomUUID();
        UUID processConfigUuid = UUID.randomUUID();

        when(processExecutionService.executeProcess(caseUuid, "user1", processConfigUuid, false)).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder request = post("/v1/execute")
            .param("caseUuid", caseUuid.toString())
            .param("processConfigUuid", processConfigUuid.toString())
            .header("userId", "user1");

        mockMvc.perform(request)
            .andExpect(status().isNotFound());

        verify(processExecutionService).executeProcess(caseUuid, "user1", processConfigUuid, false);
    }

    @Test
    void getExecutionReportsShouldReturnReports() throws Exception {
        UUID executionId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Report report = new Report(reportId, null, "root", Severity.INFO, 0, List.of());
        when(processExecutionService.getReports(executionId))
                .thenReturn(Optional.of(report));

        mockMvc.perform(get("/v1/executions/{executionId}/reports", executionId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("id").value(reportId.toString()))
            .andExpect(jsonPath("message").value("root"))
            .andExpect(jsonPath("severity").value(Severity.INFO.toString()))
            .andExpect(jsonPath("subReports").isEmpty());

        verify(processExecutionService).getReports(executionId);
    }

    @Test
    void getExecutionReportsReturnsNotFound() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(processExecutionService.getReports(executionId))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/executions/{executionId}/reports", executionId))
            .andExpect(status().isNotFound());

        verify(processExecutionService).getReports(executionId);
    }

    @Test
    void getExecutionLogsShouldReturnLogs() throws Exception {
        UUID executionId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        ReportPage reportPage = new ReportPage(1, List.of(new ReportLog("message", Severity.INFO, 1, UUID.randomUUID())), 1, 1);
        when(processExecutionService.getLogs(reportId, "filter", Set.of("INFO"), 2, 10)).thenReturn(reportPage);

        mockMvc.perform(get("/v1/executions/{executionId}/logs", executionId)
                .param("reportId", reportId.toString())
                .param("messageFilter", "filter")
                .param("severityLevelsFilter", "INFO")
                .param("page", "2")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("content[0].message").value("message"));

        verify(processExecutionService).getLogs(reportId, "filter", Set.of("INFO"), 2, 10);
    }

    @Test
    void getExecutionLogsSearchShouldReturnMatches() throws Exception {
        UUID executionId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        when(processExecutionService.getLogsSearch(reportId, null, null, "term", 20))
            .thenReturn(List.of(new MatchPosition(1, 3)));
        mockMvc.perform(get("/v1/executions/{executions}/logs/search", executionId)
                .param("reportId", reportId.toString())
                .param("searchTerm", "term")
                .param("pageSize", "20"))
            .andExpect(status().isOk()).andExpect(jsonPath("[0].page").value(1));
        verify(processExecutionService).getLogsSearch(reportId, null, null, "term", 20);
    }

    @Test
    void getExecutionReportsSeveritiesShouldReturnSeverities() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(processExecutionService.getReportsSeverities(executionId)).thenReturn(Optional.of(Set.of("INFO", "ERROR")));
        mockMvc.perform(get("/v1/executions/{executionId}/reports/aggregated-severities", executionId))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        verify(processExecutionService).getReportsSeverities(executionId);
    }

    @Test
    void getExecutionReportsSeveritiesReturnsNotFound() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(processExecutionService.getReportsSeverities(executionId)).thenReturn(Optional.empty());
        mockMvc.perform(get("/v1/executions/{executionId}/reports/aggregated-severities", executionId))
            .andExpect(status().isNotFound());

        verify(processExecutionService).getReportsSeverities(executionId);
    }

    @Test
    void getExecutionResultsShouldReturnListOfResults() throws Exception {
        UUID executionId = UUID.randomUUID();
        String result1 = "{\"result\": \"data1\"}";
        String result2 = "{\"result\": \"data2\"}";
        when(processExecutionService.getResults(executionId))
                .thenReturn(Optional.of(List.of(result1, result2)));

        mockMvc.perform(get("/v1/executions/{executionId}/results", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(result1))
                .andExpect(jsonPath("$[1]").value(result2));

        verify(processExecutionService).getResults(executionId);
    }

    @Test
    void getExecutionResultsReturnsNotFound() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(processExecutionService.getResults(executionId))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/executions/{executionId}/results", executionId))
            .andExpect(status().isNotFound());

        verify(processExecutionService).getResults(executionId);
    }

    @Test
    void getProcessExecutions() throws Exception {
        ProcessExecution processExecution1 = new ProcessExecution(UUID.randomUUID(), ProcessType.SECURITY_ANALYSIS.name(), UUID.randomUUID(), UUID.randomUUID(), ProcessStatus.COMPLETED, "env1",
                Instant.now().minusSeconds(80), Instant.now().minusSeconds(60), Instant.now().minusSeconds(30), UUID.randomUUID(), "user1", "user1");
        ProcessExecution processExecution2 = new ProcessExecution(UUID.randomUUID(), ProcessType.SECURITY_ANALYSIS.name(), UUID.randomUUID(), UUID.randomUUID(), ProcessStatus.FAILED, "env2",
                Instant.now().minusSeconds(70), Instant.now().minusSeconds(50), null, UUID.randomUUID(), "user2", "user2");
        ProcessExecution processExecution3 = new ProcessExecution(UUID.randomUUID(), ProcessType.SECURITY_ANALYSIS.name(), UUID.randomUUID(), UUID.randomUUID(), ProcessStatus.RUNNING, "env3",
                Instant.now().minusSeconds(50), Instant.now().minusSeconds(40), null, UUID.randomUUID(), "user3", "user3");

        List<ProcessExecution> processExecutionList = List.of(processExecution1, processExecution2, processExecution3);

        when(processExecutionService.getProcessExecutions()).thenReturn(processExecutionList);

        mockMvc.perform(get("/v1/executions").accept(MediaType.APPLICATION_JSON_VALUE).header("userId", "user1,user2,user3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(content().json(objectMapper.writeValueAsString(processExecutionList)));

        verify(processExecutionService).getProcessExecutions();
    }

    @Test
    void getExecutionShouldReturn() throws Exception {
        UUID executionId = UUID.randomUUID();
        ProcessExecution processExecution = mock(ProcessExecution.class);

        when(processExecutionService.getExecution(executionId)).thenReturn(Optional.of(processExecution));

        mockMvc.perform(get("/v1/executions/{executionId}", executionId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(processExecution)));

        verify(processExecutionService).getExecution(executionId);
    }

    @Test
    void getExecutionShouldReturnNotFound() throws Exception {
        UUID executionId = UUID.randomUUID();

        when(processExecutionService.getExecution(executionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/executions/{executionId}", executionId))
            .andExpect(status().isNotFound());

        verify(processExecutionService).getExecution(executionId);
    }

    @Test
    void getStepsInfos() throws Exception {
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStep processExecutionStep1 = new ProcessExecutionStep(UUID.randomUUID(), "loadNetwork", 0, StepStatus.RUNNING, null, null, Instant.now(), null);
        ProcessExecutionStep processExecutionStep2 = new ProcessExecutionStep(UUID.randomUUID(), "applyModifs", 1, StepStatus.SCHEDULED, null, null, null, null);
        ProcessExecutionStep processExecutionStep3 = new ProcessExecutionStep(UUID.randomUUID(), "runSA", 2, StepStatus.SCHEDULED, null, null, null, null);
        List<ProcessExecutionStep> processExecutionStepList = List.of(processExecutionStep1, processExecutionStep2, processExecutionStep3);

        when(processExecutionService.getStepsInfos(executionId)).thenReturn(Optional.of(processExecutionStepList));

        mockMvc.perform(get("/v1/executions/{executionId}/step-infos", executionId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(content().json(objectMapper.writeValueAsString(processExecutionStepList)));

        verify(processExecutionService).getStepsInfos(executionId);
    }

    @Test
    void getStepsInfosShouldReturn404WhenExecutionNotFound() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(processExecutionService.getStepsInfos(executionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/executions/{executionId}/step-infos", executionId))
            .andExpect(status().isNotFound());

        verify(processExecutionService).getStepsInfos(executionId);
    }

    @Test
    void deleteExecutionReturnsOK() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(processExecutionService.deleteExecution(executionId))
            .thenReturn(Optional.of(executionId));

        mockMvc.perform(delete("/v1/executions/{executionId}", executionId))
            .andExpect(status().isOk());

        verify(processExecutionService).deleteExecution(executionId);
    }

    @Test
    void deleteExecutionReturnsNotFound() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(processExecutionService.deleteExecution(executionId))
            .thenReturn(Optional.empty());

        mockMvc.perform(delete("/v1/executions/{executionId}", executionId))
            .andExpect(status().isNotFound());

        verify(processExecutionService).deleteExecution(executionId);
    }

    @Test
    void getDebugFilesReturnsOK() throws Exception {
        UUID executionId = UUID.randomUUID();
        byte[] zipContent = "dummy-zip-content".getBytes();

        when(processExecutionService.getDebugInfos(executionId))
            .thenReturn(Optional.of(zipContent));

        mockMvc.perform(get("/v1/executions/{executionId}/debug-infos", executionId))
            .andExpect(status().isOk())
            .andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"archive.zip\""))
            .andExpect(header().string(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(header().longValue(
                HttpHeaders.CONTENT_LENGTH,
                zipContent.length))
            .andExpect(content().bytes(zipContent));
    }

    @Test
    void getDebugFilesReturnsNotFound() throws Exception {
        when(processExecutionService.getDebugInfos(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/executions/{executionId}/debug-infos", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
