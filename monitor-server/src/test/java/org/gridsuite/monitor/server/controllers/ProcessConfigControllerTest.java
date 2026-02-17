/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.PersistedProcessConfig;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.SnapshotRefinerConfig;
import org.gridsuite.monitor.server.services.ProcessConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    private static Stream<Arguments> provideProcessConfig() {
        return Stream.of(
                Arguments.of(
                    new SecurityAnalysisConfig(
                        UUID.randomUUID(),
                        List.of("contingency1", "contingency2"),
                        List.of(UUID.randomUUID(), UUID.randomUUID()))
                ),
                Arguments.of(
                    new SnapshotRefinerConfig(
                        Optional.of(UUID.randomUUID()),
                        Optional.empty())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideProcessConfig")
    void createSecurityAnalysisConfig(ProcessConfig processConfig) throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.createProcessConfig(any(ProcessConfig.class)))
            .thenReturn(processConfigId);

        mockMvc.perform(post("/v1/process-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processConfig)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(processConfigId.toString()));

        verify(processConfigService).createProcessConfig(processConfig);
    }

    @ParameterizedTest
    @MethodSource("provideProcessConfig")
    void getPersistedProcessConfig(ProcessConfig processConfig) throws Exception {
        UUID processConfigId = UUID.randomUUID();
        PersistedProcessConfig persistedProcessConfig = new PersistedProcessConfig(processConfigId, processConfig);
        String expectedJson = objectMapper.writeValueAsString(persistedProcessConfig);

        when(processConfigService.getProcessConfig(any(UUID.class)))
            .thenReturn(Optional.of(persistedProcessConfig));

        mockMvc.perform(get("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        verify(processConfigService).getProcessConfig(processConfigId);
    }

    @Test
    void getProcessConfigNotFound() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.getProcessConfig(any(UUID.class)))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(processConfigService).getProcessConfig(processConfigId);
    }

    @ParameterizedTest
    @MethodSource("provideProcessConfig")
    void updateProcessConfig(ProcessConfig processConfig) throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.updateProcessConfig(any(UUID.class), any(ProcessConfig.class)))
            .thenReturn(Boolean.TRUE);

        mockMvc.perform(put("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processConfig)))
            .andExpect(status().isOk());

        verify(processConfigService).updateProcessConfig(processConfigId, processConfig);
    }

    @ParameterizedTest
    @MethodSource("provideProcessConfig")
    void updateProcessConfigNotFound(ProcessConfig processConfig) throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.updateProcessConfig(any(UUID.class), any(ProcessConfig.class)))
            .thenReturn(Boolean.FALSE);

        mockMvc.perform(put("/v1/process-configs/{uuid}", processConfigId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processConfig)))
            .andExpect(status().isNotFound());

        verify(processConfigService).updateProcessConfig(processConfigId, processConfig);
    }

    @Test
    void deleteProcessConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.deleteProcessConfig(any(UUID.class)))
            .thenReturn(Boolean.TRUE);

        mockMvc.perform(delete("/v1/process-configs/{uuid}", processConfigId))
            .andExpect(status().isOk());

        verify(processConfigService).deleteProcessConfig(processConfigId);
    }

    @Test
    void deleteProcessConfigNotFound() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.deleteProcessConfig(any(UUID.class)))
            .thenReturn(Boolean.FALSE);

        mockMvc.perform(delete("/v1/process-configs/{uuid}", processConfigId))
            .andExpect(status().isNotFound());

        verify(processConfigService).deleteProcessConfig(processConfigId);
    }

    @Test
    void getAllSecurityAnalysisConfigs() throws Exception {
        List<PersistedProcessConfig> securityAnalysisConfigs = List.of(
            new PersistedProcessConfig(UUID.randomUUID(), new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID(), UUID.randomUUID())
            )),
            new PersistedProcessConfig(UUID.randomUUID(), new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("contingency3", "contingency4"),
                List.of(UUID.randomUUID())
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
    void getAllSnapshotRefinerConfigs() throws Exception {
        List<PersistedProcessConfig> snapshotRefinerConfigs = List.of(
                new PersistedProcessConfig(UUID.randomUUID(), new SnapshotRefinerConfig(
                        Optional.of(UUID.randomUUID()),
                        Optional.of(UUID.randomUUID())
                )),
                new PersistedProcessConfig(UUID.randomUUID(), new SnapshotRefinerConfig(
                        Optional.of(UUID.randomUUID()),
                        Optional.of(UUID.randomUUID())
                ))
        );
        String expectedJson = objectMapper.writeValueAsString(snapshotRefinerConfigs);

        when(processConfigService.getProcessConfigs(ProcessType.SNAPSHOT_REFINER))
                .thenReturn(snapshotRefinerConfigs);

        mockMvc.perform(get("/v1/process-configs")
                        .param("processType", ProcessType.SNAPSHOT_REFINER.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));

        verify(processConfigService).getProcessConfigs(ProcessType.SNAPSHOT_REFINER);
    }

    @ParameterizedTest
    @EnumSource(ProcessType.class)
    void getAllProcessConfigsNotFound(ProcessType processType) throws Exception {
        when(processConfigService.getProcessConfigs(processType))
            .thenReturn(List.of());

        mockMvc.perform(get("/v1/process-configs")
                .param("processType", processType.name())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));

        verify(processConfigService).getProcessConfigs(processType);
    }
}
