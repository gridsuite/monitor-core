/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.orchestrator.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.orchestrator.server.dto.Report;
import org.gridsuite.process.orchestrator.server.dto.Severity;
import org.gridsuite.process.orchestrator.server.services.ProcessOrchestratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@WebMvcTest(ProcessOrchestratorController.class)
class ProcessOrchestratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessOrchestratorService orchestratorService;

    @Test
    void executeSecurityAnalysisShouldReturnExecutionId() throws Exception {
        UUID caseUuid = UUID.randomUUID();
        UUID parametersUuid = UUID.randomUUID();
        UUID modificationUuid = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        SecurityAnalysisConfig config = new SecurityAnalysisConfig(
                caseUuid,
                null,
                parametersUuid,
                List.of("contingency1", "contingency2"),
                List.of(modificationUuid)
        );

        when(orchestratorService.executeProcess(any(SecurityAnalysisConfig.class)))
                .thenReturn(executionId);

        mockMvc.perform(post("/v1/execute/security-analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(executionId.toString()));

        verify(orchestratorService).executeProcess(any(SecurityAnalysisConfig.class));
    }

    @Test
    void getExecutionReportsShouldReturnListOfReports() throws Exception {
        UUID executionId = UUID.randomUUID();
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();
        Report report1 = new Report(reportId1, null, "Report 1", Severity.INFO, List.of());
        Report report2 = new Report(reportId2, null, "Report 2", Severity.WARN, List.of());
        when(orchestratorService.getReports(executionId))
                .thenReturn(List.of(report1, report2));

        mockMvc.perform(get("/v1/executions/{executionId}/reports", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].message").value("Report 1"))
                .andExpect(jsonPath("$[0].severity").value("INFO"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].message").value("Report 2"))
                .andExpect(jsonPath("$[1].severity").value("WARN"));

        verify(orchestratorService).getReports(executionId);
    }

    @Test
    void getExecutionResultsShouldReturnListOfResults() throws Exception {
        UUID executionId = UUID.randomUUID();
        String result1 = "{\"result\": \"data1\"}";
        String result2 = "{\"result\": \"data2\"}";
        when(orchestratorService.getResults(executionId))
                .thenReturn(List.of(result1, result2));

        mockMvc.perform(get("/v1/executions/{executionId}/results", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(result1))
                .andExpect(jsonPath("$[1]").value(result2));

        verify(orchestratorService).getResults(executionId);
    }
}
