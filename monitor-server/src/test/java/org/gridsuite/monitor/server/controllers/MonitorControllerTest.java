/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.StepStatus;
import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.server.dto.ProcessExecution;
import org.gridsuite.monitor.server.dto.ReportLog;
import org.gridsuite.monitor.server.dto.ReportPage;
import org.gridsuite.monitor.server.dto.Severity;
import org.gridsuite.monitor.server.services.MonitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@WebMvcTest(MonitorController.class)
class MonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MonitorService monitorService;

    @Test
    void executeSecurityAnalysisShouldReturnExecutionId() throws Exception {
        UUID caseUuid = UUID.randomUUID();
        UUID parametersUuid = UUID.randomUUID();
        UUID modificationUuid = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        SecurityAnalysisConfig config = new SecurityAnalysisConfig(
                parametersUuid,
                List.of("contingency1", "contingency2"),
                List.of(modificationUuid)
        );

        when(monitorService.executeProcess(any(UUID.class), any(String.class), any(SecurityAnalysisConfig.class)))
                .thenReturn(executionId);

        mockMvc.perform(post("/v1/execute/security-analysis")
                        .param("caseUuid", caseUuid.toString())
                        .header("userId", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(executionId.toString()));

        verify(monitorService).executeProcess(eq(caseUuid), any(String.class), any(SecurityAnalysisConfig.class));
    }

    @Test
    void getExecutionReportsShouldReturnListOfReports() throws Exception {
        UUID executionId = UUID.randomUUID();
        List<ReportLog> reportLogs1 = List.of(
            new ReportLog("message1", Severity.INFO, 1, UUID.randomUUID()),
            new ReportLog("message2", Severity.WARN, 2, UUID.randomUUID()));
        ReportPage reportPage1 = new ReportPage(1, reportLogs1, 100, 10);
        List<ReportLog> reportLogs2 = List.of(new ReportLog("message3", Severity.ERROR, 3, UUID.randomUUID()));
        ReportPage reportPage2 = new ReportPage(2, reportLogs2, 200, 20);
        when(monitorService.getReports(executionId))
                .thenReturn(List.of(reportPage1, reportPage2));

        mockMvc.perform(get("/v1/executions/{executionId}/reports", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].number").value(1))
                .andExpect(jsonPath("$[0].content", hasSize(2)))
                .andExpect(jsonPath("$[0].content[0].message").value("message1"))
                .andExpect(jsonPath("$[0].content[0].severity").value(Severity.INFO.toString()))
                .andExpect(jsonPath("$[0].content[0].depth").value(1))
                .andExpect(jsonPath("$[0].content[1].message").value("message2"))
                .andExpect(jsonPath("$[0].content[1].severity").value(Severity.WARN.toString()))
                .andExpect(jsonPath("$[0].content[1].depth").value(2))
                .andExpect(jsonPath("$[0].totalElements").value(100))
                .andExpect(jsonPath("$[0].totalPages").value(10))
                .andExpect(jsonPath("$[1].number").value(2))
                .andExpect(jsonPath("$[1].content", hasSize(1)))
                .andExpect(jsonPath("$[1].content[0].message").value("message3"))
                .andExpect(jsonPath("$[1].content[0].severity").value(Severity.ERROR.toString()))
                .andExpect(jsonPath("$[1].content[0].depth").value(3))
                .andExpect(jsonPath("$[1].totalElements").value(200))
                .andExpect(jsonPath("$[1].totalPages").value(20));

        verify(monitorService).getReports(executionId);
    }

    @Test
    void getExecutionResultsShouldReturnListOfResults() throws Exception {
        UUID executionId = UUID.randomUUID();
        String result1 = "{\"result\": \"data1\"}";
        String result2 = "{\"result\": \"data2\"}";
        when(monitorService.getResults(executionId))
                .thenReturn(List.of(result1, result2));

        mockMvc.perform(get("/v1/executions/{executionId}/results", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(result1))
                .andExpect(jsonPath("$[1]").value(result2));

        verify(monitorService).getResults(executionId);
    }

    @Test
    void getLaunchedProcesses() throws Exception {
        ProcessExecution processExecution1 = new ProcessExecution(UUID.randomUUID(), ProcessType.SECURITY_ANALYSIS.name(), UUID.randomUUID(), ProcessStatus.COMPLETED, "env1", Instant.now().minusSeconds(80), Instant.now().minusSeconds(60), Instant.now().minusSeconds(30), "user1");
        ProcessExecution processExecution2 = new ProcessExecution(UUID.randomUUID(), ProcessType.SECURITY_ANALYSIS.name(), UUID.randomUUID(), ProcessStatus.FAILED, "env2", Instant.now().minusSeconds(70), Instant.now().minusSeconds(50), null, "user2");
        ProcessExecution processExecution3 = new ProcessExecution(UUID.randomUUID(), ProcessType.SECURITY_ANALYSIS.name(), UUID.randomUUID(), ProcessStatus.RUNNING, "env3", Instant.now().minusSeconds(50), Instant.now().minusSeconds(40), null, "user3");

        List<ProcessExecution> processExecutionList = List.of(processExecution1, processExecution2, processExecution3);

        when(monitorService.getLaunchedProcesses(ProcessType.SECURITY_ANALYSIS)).thenReturn(processExecutionList);

        mockMvc.perform(get("/v1/executions?processType=SECURITY_ANALYSIS").accept(MediaType.APPLICATION_JSON_VALUE).header("userId", "user1,user2,user3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(content().json(objectMapper.writeValueAsString(processExecutionList)));

        verify(monitorService).getLaunchedProcesses(ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void getStepsInfos() throws Exception {
        UUID executionId = UUID.randomUUID();
        ProcessExecutionStep processExecutionStep1 = new ProcessExecutionStep(UUID.randomUUID(), "loadNetwork", 0, StepStatus.RUNNING, null, null, UUID.randomUUID(), Instant.now(), null);
        ProcessExecutionStep processExecutionStep2 = new ProcessExecutionStep(UUID.randomUUID(), "applyModifs", 1, StepStatus.SCHEDULED, null, null, UUID.randomUUID(), null, null);
        ProcessExecutionStep processExecutionStep3 = new ProcessExecutionStep(UUID.randomUUID(), "runSA", 2, StepStatus.SCHEDULED, null, null, UUID.randomUUID(), null, null);
        List<ProcessExecutionStep> processExecutionStepList = List.of(processExecutionStep1, processExecutionStep2, processExecutionStep3);

        when(monitorService.getStepsInfos(executionId)).thenReturn(Optional.of(processExecutionStepList));

        mockMvc.perform(get("/v1/executions/{executionId}/step-infos", executionId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(content().json(objectMapper.writeValueAsString(processExecutionStepList)));

        verify(monitorService).getStepsInfos(executionId);
    }

    @Test
    void getStepsInfosShouldReturn404WhenExecutionNotFound() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(monitorService.getStepsInfos(executionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/executions/{executionId}/step-infos", executionId))
            .andExpect(status().isNotFound());

        verify(monitorService).getStepsInfos(executionId);
    }

    @Test
    void deleteExecutionShouldReturnTrue() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(monitorService.deleteExecution(executionId))
            .thenReturn(Boolean.TRUE);

        mockMvc.perform(delete("/v1/executions/{executionId}", executionId))
            .andExpect(status().isOk());

        verify(monitorService).deleteExecution(executionId);
    }

    @Test
    void deleteExecutionShouldReturnFalse() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(monitorService.deleteExecution(executionId))
            .thenReturn(Boolean.FALSE);

        mockMvc.perform(delete("/v1/executions/{executionId}", executionId))
            .andExpect(status().isNotFound());

        verify(monitorService).deleteExecution(executionId);
    }
}
