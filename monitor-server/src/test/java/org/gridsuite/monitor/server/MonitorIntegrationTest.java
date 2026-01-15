/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.*;
import org.gridsuite.monitor.server.dto.Report;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.gridsuite.monitor.server.services.ConsumerService;
import org.gridsuite.monitor.server.services.DummyReportService;
import org.gridsuite.monitor.server.services.MonitorService;
import org.gridsuite.monitor.server.services.ResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@SpringBootTest(classes = {MonitorServerApplication.class, TestChannelBinderConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class MonitorIntegrationTest {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private ProcessExecutionRepository executionRepository;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DummyReportService reportService;

    @MockitoBean
    private ResultService resultService;

    private UUID caseUuid;

    public static final String PROCESS_SA_RUN_DESTINATION = "monitor.process.securityanalysis.run";

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
    }

    @Test
    void securityAnalysisProcessIT() throws Exception {
        // Start process execution
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
                caseUuid,
                null,
                UUID.randomUUID(),
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID()));
        UUID executionId = monitorService.executeProcess(securityAnalysisConfig);

        // Verify message was published
        Message<byte[]> sentMessage = outputDestination.receive(1000, PROCESS_SA_RUN_DESTINATION);
        assertThat(sentMessage).isNotNull();

        // Verify execution persisted with correct initial state
        ProcessExecutionEntity execution = executionRepository.findById(executionId).orElse(null);
        assertThat(execution).isNotNull();
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.SCHEDULED);
        assertThat(execution.getSteps()).isEmpty();

        // Simulate first step creation via message with both report and result
        UUID stepId1 = UUID.randomUUID();
        UUID reportId1 = UUID.randomUUID();
        UUID resultId1 = UUID.randomUUID();
        ProcessExecutionStep processExecutionStep = ProcessExecutionStep.builder()
                .id(stepId1)
                .stepType("LOAD_NETWORK")
                .stepOrder(0)
                .status(StepStatus.COMPLETED)
                .reportId(reportId1)
                .resultId(resultId1)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .build();

        Message<String> stepMessage1 = MessageBuilder
                .withPayload(objectMapper.writeValueAsString(processExecutionStep))
                .setHeader(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.STEP_STATUS_UPDATE.toString())
                .setHeader(ConsumerService.HEADER_EXECUTION_ID, executionId.toString())
                .build();

        consumerService.consumeMonitorUpdate().accept(stepMessage1);

        // Simulate second step creation via message with both report and result
        UUID stepId2 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();
        UUID resultId2 = UUID.randomUUID();
        ProcessExecutionStep stepDto2 = ProcessExecutionStep.builder()
                .id(stepId2)
                .stepType("SECURITY_ANALYSIS")
                .stepOrder(1)
                .status(StepStatus.COMPLETED)
                .reportId(reportId2)
                .resultId(resultId2)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .build();

        Message<String> stepMessage2 = MessageBuilder
                .withPayload(objectMapper.writeValueAsString(stepDto2))
                .setHeader(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.STEP_STATUS_UPDATE.toString())
                .setHeader(ConsumerService.HEADER_EXECUTION_ID, executionId.toString())
                .build();

        consumerService.consumeMonitorUpdate().accept(stepMessage2);

        // Verify both steps were added to database with correct data
        execution = executionRepository.findById(executionId).orElse(null);
        assertThat(execution.getSteps()).hasSize(2);
        assertThat(execution.getSteps().get(0).getId()).isEqualTo(stepId1);
        assertThat(execution.getSteps().get(0).getStatus()).isEqualTo(StepStatus.COMPLETED);
        assertThat(execution.getSteps().get(0).getReportId()).isEqualTo(reportId1);
        assertThat(execution.getSteps().get(0).getResultId()).isEqualTo(resultId1);
        assertThat(execution.getSteps().get(1).getId()).isEqualTo(stepId2);
        assertThat(execution.getSteps().get(1).getReportId()).isEqualTo(reportId2);
        assertThat(execution.getSteps().get(1).getResultId()).isEqualTo(resultId2);

        // Complete the execution via message
        ProcessExecutionStatusUpdate finalStatus = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.COMPLETED)
                .executionEnvName("test-env")
                .completedAt(Instant.now())
                .build();

        Message<String> statusMessage = MessageBuilder
                .withPayload(objectMapper.writeValueAsString(finalStatus))
                .setHeader(ConsumerService.HEADER_MESSAGE_TYPE, MessageType.EXECUTION_STATUS_UPDATE.toString())
                .setHeader(ConsumerService.HEADER_EXECUTION_ID, executionId.toString())
                .build();

        consumerService.consumeMonitorUpdate().accept(statusMessage);

        // Verify final state persisted
        execution = executionRepository.findById(executionId).orElse(null);
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.COMPLETED);
        assertThat(execution.getExecutionEnvName()).isEqualTo("test-env");

        // Mock the report service responses
        Report report1 = new Report(reportId1, null, "Load Network Report", null, List.of());
        Report report2 = new Report(reportId2, null, "Security Analysis Report", null, List.of());
        when(reportService.getReport(reportId1)).thenReturn(report1);
        when(reportService.getReport(reportId2)).thenReturn(report2);

        // Test the reports endpoint fetches correctly from database
        mockMvc.perform(get("/v1/executions/{executionId}/reports", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].message").value("Load Network Report"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].message").value("Security Analysis Report"));

        // Mock the result service responses
        String result1 = "{\"result\": \"success\"}";
        String result2 = "{\"securityAnalysisResult\": \"violations found\"}";
        when(resultService.getResult(new ResultInfos(resultId1, ResultType.SECURITY_ANALYSIS))).thenReturn(result1);
        when(resultService.getResult(new ResultInfos(resultId2, ResultType.SECURITY_ANALYSIS))).thenReturn(result2);

        // Test the results endpoint fetches correctly from database
        mockMvc.perform(get("/v1/executions/{executionId}/results", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(result1))
                .andExpect(jsonPath("$[1]").value(result2));
    }
}
