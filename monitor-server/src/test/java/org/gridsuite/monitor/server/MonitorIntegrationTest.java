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
import org.gridsuite.monitor.server.services.*;
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

    @MockitoBean
    private S3RestService s3RestService;

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
                List.of(UUID.randomUUID()));
        UUID executionId = monitorService.executeProcess(caseUuid, userId, securityAnalysisConfig, false);

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
                List.of(modificationUuid)
        );
        UUID configId = configService.createProcessConfig(securityAnalysisConfig);
        assertThat(processConfigRepository.findById(configId)).isNotEmpty();

        Optional<PersistedProcessConfig> config = configService.getProcessConfig(configId);
        assertThat(config).isNotEmpty();
        assertThat(config.get().processConfig()).usingRecursiveComparison().isEqualTo(securityAnalysisConfig);

        UUID updatedParametersUuid = UUID.randomUUID();
        UUID updatedModificationUuid = UUID.randomUUID();
        SecurityAnalysisConfig updatedSecurityAnalysisConfig = new SecurityAnalysisConfig(
                updatedParametersUuid,
                List.of("contingency3", "contingency4"),
                List.of(updatedModificationUuid)
        );
        boolean updated = configService.updateProcessConfig(configId, updatedSecurityAnalysisConfig);
        assertThat(updated).isTrue();
        Optional<PersistedProcessConfig> updatedConfig = configService.getProcessConfig(configId);
        assertThat(updatedConfig).isNotEmpty();
        assertThat(updatedConfig.get().processConfig()).usingRecursiveComparison().isEqualTo(updatedSecurityAnalysisConfig);

        boolean deleted = configService.deleteProcessConfig(configId);
        assertThat(deleted).isTrue();
        Optional<PersistedProcessConfig> deletedConfig = configService.getProcessConfig(configId);
        assertThat(deletedConfig).isEmpty();
    }

    @Test
    void processConfigsIT() {
        UUID parametersUuid1 = UUID.randomUUID();
        UUID modificationUuid1 = UUID.randomUUID();
        SecurityAnalysisConfig securityAnalysisConfig1 = new SecurityAnalysisConfig(
            parametersUuid1,
            List.of("contingency1", "contingency2"),
            List.of(modificationUuid1)
        );
        UUID configId1 = configService.createProcessConfig(securityAnalysisConfig1);
        assertThat(processConfigRepository.findById(configId1)).isNotEmpty();

        UUID parametersUuid2 = UUID.randomUUID();
        UUID modificationUuid2 = UUID.randomUUID();
        SecurityAnalysisConfig securityAnalysisConfig2 = new SecurityAnalysisConfig(
            parametersUuid2,
            List.of("contingency3", "contingency4"),
            List.of(modificationUuid2)
        );
        UUID configId2 = configService.createProcessConfig(securityAnalysisConfig2);
        assertThat(processConfigRepository.findById(configId2)).isNotEmpty();

        List<PersistedProcessConfig> processConfigs = configService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);
        assertThat(processConfigs).hasSize(2);

        PersistedProcessConfig retrievedConfig1 = processConfigs.stream()
            .filter(c -> c.id().equals(configId1))
            .findFirst()
            .orElseThrow();
        SecurityAnalysisConfig retrievedSecurityAnalysisConfig1 = (SecurityAnalysisConfig) retrievedConfig1.processConfig();

        PersistedProcessConfig retrievedConfig2 = processConfigs.stream()
            .filter(c -> c.id().equals(configId2))
            .findFirst()
            .orElseThrow();
        SecurityAnalysisConfig retrievedSecurityAnalysisConfig2 = (SecurityAnalysisConfig) retrievedConfig2.processConfig();

        assertThat(retrievedSecurityAnalysisConfig1.parametersUuid()).isEqualTo(parametersUuid1);
        assertThat(retrievedSecurityAnalysisConfig1.contingencies()).isEqualTo(List.of("contingency1", "contingency2"));
        assertThat(retrievedSecurityAnalysisConfig1.modificationUuids()).isEqualTo(List.of(modificationUuid1));

        assertThat(retrievedSecurityAnalysisConfig2.parametersUuid()).isEqualTo(parametersUuid2);
        assertThat(retrievedSecurityAnalysisConfig2.contingencies()).isEqualTo(List.of("contingency3", "contingency4"));
        assertThat(retrievedSecurityAnalysisConfig2.modificationUuids()).isEqualTo(List.of(modificationUuid2));

        boolean deleted = configService.deleteProcessConfig(configId1);
        assertThat(deleted).isTrue();

        List<PersistedProcessConfig> remainingConfigs = configService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);
        assertThat(remainingConfigs).hasSize(1);
        assertThat(remainingConfigs.get(0).processConfig().processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);

        boolean deletedSecond = configService.deleteProcessConfig(configId2);
        assertThat(deletedSecond).isTrue();

        List<PersistedProcessConfig> noConfigs = configService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS);
        assertThat(noConfigs).isEmpty();
    }
}
