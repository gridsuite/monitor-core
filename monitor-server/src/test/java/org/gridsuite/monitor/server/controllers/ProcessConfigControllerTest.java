/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.server.dto.processconfig.MetadataInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.services.processconfig.ProcessConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@WebMvcTest(ProcessConfigController.class)
class ProcessConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessConfigService processConfigService;

    @Test
    void createSecurityAnalysisConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID(), UUID.randomUUID()),
                UUID.randomUUID()
        );

        when(processConfigService.createProcessConfig(any(ProcessConfig.class)))
            .thenReturn(processConfigId);

        mockMvc.perform(post("/v1/process-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(securityAnalysisConfig)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(processConfigId.toString()));

        verify(processConfigService).createProcessConfig(any(SecurityAnalysisConfig.class));
    }

    @Test
    void getSecurityAnalysisConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();
        PersistedProcessConfig securityAnalysisConfig = new PersistedProcessConfig(UUID.randomUUID(), new SecurityAnalysisConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID()),
            UUID.randomUUID()
        ));

        String expectedJson = objectMapper.writeValueAsString(securityAnalysisConfig);

        when(processConfigService.getProcessConfig(any(UUID.class)))
            .thenReturn(Optional.of(securityAnalysisConfig));

        mockMvc.perform(get("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        verify(processConfigService).getProcessConfig(any(UUID.class));
    }

    @Test
    void getSecurityAnalysisConfigNotFound() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.getProcessConfig(any(UUID.class)))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(processConfigService).getProcessConfig(any(UUID.class));
    }

    @Test
    void getSecurityAnalysisConfigsMetadata() throws Exception {
        UUID processConfigId1 = UUID.randomUUID();
        UUID processConfigId2 = UUID.randomUUID();

        List<MetadataInfos> expectedMetadata = List.of(
            new MetadataInfos(processConfigId1, ProcessType.SECURITY_ANALYSIS),
            new MetadataInfos(processConfigId2, ProcessType.SECURITY_ANALYSIS)
        );

        when(processConfigService.getProcessConfigsMetadata(List.of(processConfigId1, processConfigId2)))
            .thenReturn(expectedMetadata);

        mockMvc.perform(get("/v1/process-configs/metadata")
                .param("ids", processConfigId1.toString(), processConfigId2.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(expectedMetadata)));

        verify(processConfigService).getProcessConfigsMetadata(List.of(processConfigId1, processConfigId2));
    }

    @Test
    void updateSecurityAnalysisConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID()),
            UUID.randomUUID()
        );

        when(processConfigService.updateProcessConfig(any(UUID.class), any(ProcessConfig.class)))
            .thenReturn(Boolean.TRUE);

        mockMvc.perform(put("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(securityAnalysisConfig)))
            .andExpect(status().isOk());

        verify(processConfigService).updateProcessConfig(any(UUID.class), any(ProcessConfig.class));
    }

    @Test
    void updateSecurityAnalysisConfigNotFound() throws Exception {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID()),
            UUID.randomUUID()
        );

        when(processConfigService.updateProcessConfig(any(UUID.class), any(ProcessConfig.class)))
            .thenReturn(Boolean.FALSE);

        mockMvc.perform(put("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(securityAnalysisConfig)))
            .andExpect(status().isNotFound());

        verify(processConfigService).updateProcessConfig(any(UUID.class), any(ProcessConfig.class));
    }

    @Test
    void duplicateProcessConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();
        UUID newProcessConfigId = UUID.randomUUID();

        when(processConfigService.duplicateProcessConfig(processConfigId))
            .thenReturn(Optional.of(newProcessConfigId));

        mockMvc.perform(post("/v1/process-configs/duplication?duplicateFrom={uuid}", processConfigId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(newProcessConfigId.toString()));

        verify(processConfigService).duplicateProcessConfig(processConfigId);
    }

    @Test
    void duplicateProcessConfigNotFound() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.duplicateProcessConfig(processConfigId))
            .thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/process-configs/duplication?duplicateFrom={uuid}", processConfigId))
            .andExpect(status().isNotFound());

        verify(processConfigService).duplicateProcessConfig(processConfigId);
    }

    @Test
    void deleteSecurityAnalysisConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.deleteProcessConfig(any(UUID.class)))
            .thenReturn(Boolean.TRUE);

        mockMvc.perform(delete("/v1/process-configs/{uuid}", processConfigId))
            .andExpect(status().isOk());

        verify(processConfigService).deleteProcessConfig(any(UUID.class));
    }

    @Test
    void deleteSecurityAnalysisConfigNotFound() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.deleteProcessConfig(any(UUID.class)))
            .thenReturn(Boolean.FALSE);

        mockMvc.perform(delete("/v1/process-configs/{uuid}", processConfigId))
            .andExpect(status().isNotFound());

        verify(processConfigService).deleteProcessConfig(any(UUID.class));
    }

    @Test
    void getAllSecurityAnalysisConfigs() throws Exception {
        List<PersistedProcessConfig> securityAnalysisConfigs = List.of(
            new PersistedProcessConfig(UUID.randomUUID(), new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID(), UUID.randomUUID()),
                UUID.randomUUID()
            )),
            new PersistedProcessConfig(UUID.randomUUID(), new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                UUID.randomUUID()
            ))
        );
        String expectedJson = objectMapper.writeValueAsString(securityAnalysisConfigs);

        when(processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS))
            .thenReturn(securityAnalysisConfigs);

        mockMvc.perform(get("/v1/process-configs")
                .param("processType", ProcessType.SECURITY_ANALYSIS.name())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        verify(processConfigService).getProcessConfigs(ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void getAllSecurityAnalysisConfigsNotFound() throws Exception {
        when(processConfigService.getProcessConfigs(ProcessType.SECURITY_ANALYSIS))
            .thenReturn(List.of());

        mockMvc.perform(get("/v1/process-configs")
                .param("processType", ProcessType.SECURITY_ANALYSIS.name())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));

        verify(processConfigService).getProcessConfigs(ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void compareProcessConfigsShouldReturnComparisonResult() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID());

        ProcessConfigComparison comparison = new ProcessConfigComparison(
            uuid1,
            uuid2,
            true,
            List.of(
                new ProcessConfigFieldComparison("modifications", true, modificationUuids, modificationUuids),
                new ProcessConfigFieldComparison("securityAnalysisParameters", true, securityAnalysisParametersUuid, securityAnalysisParametersUuid),
                new ProcessConfigFieldComparison("loadflowParameters", true, loadflowParametersUuid, loadflowParametersUuid)
            )
        );

        when(processConfigService.compareProcessConfigs(uuid1, uuid2))
            .thenReturn(Optional.of(comparison));

        mockMvc.perform(get("/v1/process-configs/compare")
                .param("uuid1", uuid1.toString())
                .param("uuid2", uuid2.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.processConfigUuid1").value(uuid1.toString()))
            .andExpect(jsonPath("$.processConfigUuid2").value(uuid2.toString()))
            .andExpect(jsonPath("$.identical").value(true))
            .andExpect(jsonPath("$.differences").isArray())
            .andExpect(jsonPath("$.differences.length()").value(3));

        verify(processConfigService).compareProcessConfigs(uuid1, uuid2);
    }

    @Test
    void compareProcessConfigsShouldReturnDifferences() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(UUID.randomUUID());
        List<UUID> modificationUuids2 = List.of(UUID.randomUUID());

        ProcessConfigComparison comparison = new ProcessConfigComparison(
            uuid1,
            uuid2,
            false,
            List.of(
                new ProcessConfigFieldComparison("modifications", false, modificationUuids1, modificationUuids2)
            )
        );

        when(processConfigService.compareProcessConfigs(uuid1, uuid2))
            .thenReturn(Optional.of(comparison));

        mockMvc.perform(get("/v1/process-configs/compare")
                .param("uuid1", uuid1.toString())
                .param("uuid2", uuid2.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.identical").value(false))
            .andExpect(jsonPath("$.differences[0].field").value("modifications"))
            .andExpect(jsonPath("$.differences[0].identical").value(false));

        verify(processConfigService).compareProcessConfigs(uuid1, uuid2);
    }

    @Test
    void compareProcessConfigsShouldReturn404WhenConfigNotFound() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        when(processConfigService.compareProcessConfigs(uuid1, uuid2))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/process-configs/compare")
                .param("uuid1", uuid1.toString())
                .param("uuid2", uuid2.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(processConfigService).compareProcessConfigs(uuid1, uuid2);
    }

    @Test
    void compareProcessConfigsShouldReturn400WhenDifferentTypes() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        when(processConfigService.compareProcessConfigs(any(), any()))
            .thenThrow(new IllegalArgumentException("Cannot compare different process config types"));

        mockMvc.perform(get("/v1/process-configs/compare")
                .param("uuid1", uuid1.toString())
                .param("uuid2", uuid2.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(processConfigService).compareProcessConfigs(uuid1, uuid2);
    }

    @Test
    void compareProcessConfigsShouldReturn400WhenMissingParameters() throws Exception {
        mockMvc.perform(get("/v1/process-configs/compare")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void compareProcessConfigsShouldReturn400WhenInvalidUUID() throws Exception {
        mockMvc.perform(get("/v1/process-configs/compare")
                .param("uuid1", "invalid-uuid")
                .param("uuid2", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
