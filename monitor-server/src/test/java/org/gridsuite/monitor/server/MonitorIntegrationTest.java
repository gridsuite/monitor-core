/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.*;
import org.gridsuite.monitor.server.dto.ReportLog;
import org.gridsuite.monitor.server.dto.ReportPage;
import org.gridsuite.monitor.server.dto.Severity;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.gridsuite.monitor.server.repositories.ProcessExecutionRepository;
import org.gridsuite.monitor.server.services.ConsumerService;
import org.gridsuite.monitor.server.services.ProcessConfigService;
import org.gridsuite.monitor.server.services.ReportService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
    private ProcessConfigService configService;

    @Autowired
    private ProcessExecutionRepository executionRepository;

    @Autowired
    private ProcessConfigRepository processConfigRepository;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private ResultService resultService;

    private UUID caseUuid;

    private String userId;

    public static final String PROCESS_SA_RUN_DESTINATION = "monitor.process.securityanalysis.run";

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        userId = "user1";
    }

    @Test
    void securityAnalysisProcessIT() throws Exception {
        // Start process execution
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID()),
                null, null, null, null);
        UUID executionId = monitorService.executeProcess(caseUuid, userId, securityAnalysisConfig);

        // Verify message was published
        Message<byte[]> sentMessage = outputDestination.receive(1000, PROCESS_SA_RUN_DESTINATION);
        assertThat(sentMessage).isNotNull();

        // Verify execution persisted with correct initial state
        ProcessExecutionEntity execution = executionRepository.findById(executionId).orElse(null);
        assertThat(execution).isNotNull();
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.SCHEDULED);
        assertThat(execution.getSteps()).isEmpty();

        // Simulate first step creation via message with both report and result
        UUID stepId0 = UUID.randomUUID();
        UUID reportId0 = UUID.randomUUID();
        UUID resultId0 = UUID.randomUUID();
        ProcessExecutionStep step0 = ProcessExecutionStep.builder()
                .id(stepId0)
                .stepType("LOAD_NETWORK")
                .stepOrder(0)
                .status(StepStatus.COMPLETED)
                .reportId(reportId0)
                .resultId(resultId0)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .build();
        sendMessage(executionId, step0, MessageType.STEP_STATUS_UPDATE);

        // Simulate second step creation via message with both report and result
        UUID stepId1 = UUID.randomUUID();
        UUID reportId1 = UUID.randomUUID();
        UUID resultId1 = UUID.randomUUID();
        ProcessExecutionStep step1 = ProcessExecutionStep.builder()
                .id(stepId1)
                .stepType("SECURITY_ANALYSIS")
                .stepOrder(1)
                .status(StepStatus.COMPLETED)
                .reportId(reportId1)
                .resultId(resultId1)
                .resultType(ResultType.SECURITY_ANALYSIS)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .build();
        sendMessage(executionId, step1, MessageType.STEP_STATUS_UPDATE);

        // Verify both steps were added to database with correct data
        execution = executionRepository.findById(executionId).orElse(null);
        assertThat(execution).isNotNull();
        assertThat(execution.getSteps()).hasSize(2);
        assertThat(execution.getSteps().get(0).getId()).isEqualTo(stepId0);
        assertThat(execution.getSteps().get(0).getStatus()).isEqualTo(StepStatus.COMPLETED);
        assertThat(execution.getSteps().get(0).getReportId()).isEqualTo(reportId0);
        assertThat(execution.getSteps().get(0).getResultId()).isEqualTo(resultId0);
        assertThat(execution.getSteps().get(1).getId()).isEqualTo(stepId1);
        assertThat(execution.getSteps().get(1).getReportId()).isEqualTo(reportId1);
        assertThat(execution.getSteps().get(1).getResultId()).isEqualTo(resultId1);

        // Complete the execution via message
        Instant startedAt = Instant.now();
        Instant completedAt = Instant.now();
        ProcessExecutionStatusUpdate finalStatus = ProcessExecutionStatusUpdate.builder()
                .status(ProcessStatus.COMPLETED)
                .executionEnvName("test-env")
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
        sendMessage(executionId, finalStatus, MessageType.EXECUTION_STATUS_UPDATE);

        // Verify final state persisted
        execution = executionRepository.findById(executionId).orElse(null);
        assertThat(execution).isNotNull();
        assertThat(execution.getStatus()).isEqualTo(ProcessStatus.COMPLETED);
        assertThat(execution.getExecutionEnvName()).isEqualTo("test-env");
        assertThat(execution.getStartedAt().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(startedAt.truncatedTo(ChronoUnit.MILLIS));
        assertThat(execution.getCompletedAt().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(completedAt.truncatedTo(ChronoUnit.MILLIS));

        // Mock the report service responses
        ReportPage reportPage0 = new ReportPage(1, List.of(
            new ReportLog("message1", Severity.INFO, 1, UUID.randomUUID()),
            new ReportLog("message2", Severity.WARN, 2, UUID.randomUUID())), 100, 10);
        ReportPage reportPage1 = new ReportPage(2, List.of(new ReportLog("message3", Severity.ERROR, 3, UUID.randomUUID())), 200, 20);

        when(reportService.getReport(reportId0)).thenReturn(reportPage0);
        when(reportService.getReport(reportId1)).thenReturn(reportPage1);

        // Test the reports endpoint fetches correctly from database
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

        // Mock the result service responses
        String result0 = "{\"result\": \"success\"}";
        String result1 = "{\"securityAnalysisResult\": \"violations found\"}";
        when(resultService.getResult(new ResultInfos(resultId0, ResultType.SECURITY_ANALYSIS))).thenReturn(result0);
        when(resultService.getResult(new ResultInfos(resultId1, ResultType.SECURITY_ANALYSIS))).thenReturn(result1);

        // Test the results endpoint fetches correctly from database
        mockMvc.perform(get("/v1/executions/{executionId}/results", executionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value(result0))
                .andExpect(jsonPath("$[1]").value(result1));
    }

    private void sendMessage(UUID executionId, Object step, MessageType messageType) throws Exception {
        Message<String> message = MessageBuilder
                .withPayload(objectMapper.writeValueAsString(step))
                .setHeader(ConsumerService.HEADER_MESSAGE_TYPE, messageType.toString())
                .setHeader(ConsumerService.HEADER_EXECUTION_ID, executionId.toString())
                .build();
        consumerService.consumeMonitorUpdate().accept(message);
    }

    @Test
    void processConfigIT() {
        UUID parametersUuid = UUID.randomUUID();
        UUID modificationUuid = UUID.randomUUID();
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
                parametersUuid,
                List.of("contingency1", "contingency2"),
                List.of(modificationUuid),
                null, null, null, null
        );
        UUID configId = configService.createProcessConfig(securityAnalysisConfig, "user1");
        assertThat(processConfigRepository.findById(configId)).isNotEmpty();

        Optional<ProcessConfig> config = configService.getProcessConfig(configId);
        assertThat(config).isNotEmpty();
        assertThat(config.get().getOwner()).isEqualTo("user1");
        assertThat(config.get().getLastModifiedBy()).isEqualTo("user1");
        assertThat(config.get().getCreationDate()).isNotNull();
        assertThat(config.get().getLastModificationDate()).isNotNull();

        UUID updatedParametersUuid = UUID.randomUUID();
        UUID updatedModificationUuid = UUID.randomUUID();
        SecurityAnalysisConfig updatedSecurityAnalysisConfig = new SecurityAnalysisConfig(
                updatedParametersUuid,
                List.of("contingency3", "contingency4"),
                List.of(updatedModificationUuid),
                null, null, null, null
        );
        boolean updated = configService.updateProcessConfig(configId, updatedSecurityAnalysisConfig, "user2");
        assertThat(updated).isTrue();
        Optional<ProcessConfig> updatedConfig = configService.getProcessConfig(configId);
        assertThat(updatedConfig).isNotEmpty();
        assertThat(updatedConfig.get().getOwner()).isEqualTo("user1");
        assertThat(updatedConfig.get().getLastModifiedBy()).isEqualTo("user2");
        assertThat(updatedConfig.get().getLastModificationDate()).isNotNull();

        boolean deleted = configService.deleteProcessConfig(configId);
        assertThat(deleted).isTrue();
        Optional<ProcessConfig> deletedConfig = configService.getProcessConfig(configId);
        assertThat(deletedConfig).isEmpty();
    }
}
