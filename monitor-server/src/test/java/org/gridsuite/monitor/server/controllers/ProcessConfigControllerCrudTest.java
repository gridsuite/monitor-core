package org.gridsuite.monitor.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;
import org.gridsuite.monitor.server.services.processconfig.ProcessConfigService;
import org.gridsuite.monitor.server.testdata.processconfig.ProcessConfigTestDataProvider;
import org.gridsuite.monitor.server.testdata.processconfig.ProcessConfigTestDataProviders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessConfigController.class)
class ProcessConfigControllerCrudTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessConfigService processConfigService;

    static Stream<ProcessConfigTestDataProvider<?, ?>> providerStream() {
        return ProcessConfigTestDataProviders.all().stream();
    }

    @ParameterizedTest
    @MethodSource("providerStream")
    void createProcessConfig(ProcessConfigTestDataProvider<ProcessConfig, ProcessConfigEntity> provider) throws Exception {
        UUID processConfigId = UUID.randomUUID();
        ProcessConfig dto = provider.createDto();

        when(processConfigService.createProcessConfig(any(provider.dtoType())))
                .thenReturn(processConfigId);

        mockMvc.perform(post("/v1/process-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(processConfigId.toString()));

        verify(processConfigService).createProcessConfig(any(provider.dtoType()));
    }

    @ParameterizedTest
    @MethodSource("providerStream")
    void updateProcessConfig(ProcessConfigTestDataProvider<ProcessConfig, ProcessConfigEntity> provider) throws Exception {
        UUID processConfigId = UUID.randomUUID();
        ProcessConfig processConfig = provider.createDto();

        when(processConfigService.updateProcessConfig(any(UUID.class), any(provider.dtoType())))
                .thenReturn(Boolean.TRUE);

        mockMvc.perform(put("/v1/process-configs/{uuid}", processConfigId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processConfig)))
                .andExpect(status().isOk());

        verify(processConfigService).updateProcessConfig(any(UUID.class), any(provider.dtoType()));
    }

    @ParameterizedTest
    @MethodSource("providerStream")
    void getSecurityAnalysisConfig(ProcessConfigTestDataProvider<ProcessConfig, ProcessConfigEntity> provider) throws Exception {
        UUID processConfigId = UUID.randomUUID();
        PersistedProcessConfig persistedProcessConfig = new PersistedProcessConfig(UUID.randomUUID(), provider.createDto());

        String expectedJson = objectMapper.writeValueAsString(persistedProcessConfig);

        when(processConfigService.getProcessConfig(any(UUID.class)))
                .thenReturn(Optional.of(persistedProcessConfig));

        mockMvc.perform(get("/v1/process-configs/{uuid}", processConfigId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));

        verify(processConfigService).getProcessConfig(any(UUID.class));
    }

    @Test
    void deleteProcessConfig() throws Exception {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigService.deleteProcessConfig(any(UUID.class)))
                .thenReturn(Boolean.TRUE);

        mockMvc.perform(delete("/v1/process-configs/{uuid}", processConfigId))
                .andExpect(status().isOk());

        verify(processConfigService).deleteProcessConfig(any(UUID.class));
    }

}
