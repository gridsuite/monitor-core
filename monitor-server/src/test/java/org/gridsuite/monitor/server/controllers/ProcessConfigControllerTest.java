/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.api.types.processconfig.SecurityAnalysisConfig;
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
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID(), UUID.randomUUID())
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
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
            UUID.randomUUID(),
            List.of("contingency1", "contingency2"),
            List.of(UUID.randomUUID(), UUID.randomUUID())
        );
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
    void updateSecurityAnalysisConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(
            UUID.randomUUID(),
            List.of("contingency1", "contingency2"),
            List.of(UUID.randomUUID(), UUID.randomUUID())
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
            List.of("contingency1", "contingency2"),
            List.of(UUID.randomUUID(), UUID.randomUUID())
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
}
