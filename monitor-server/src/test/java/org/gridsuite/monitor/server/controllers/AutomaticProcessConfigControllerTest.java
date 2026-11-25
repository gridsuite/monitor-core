/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.PropertyServerNameProvider;
import org.gridsuite.monitor.server.dto.processconfig.AutomaticProcessConfigInfos;
import org.gridsuite.monitor.server.services.automaticprocessconfig.AutomaticProcessConfigService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@WebMvcTest(controllers = {AutomaticProcessConfigController.class, PropertyServerNameProvider.class})
class AutomaticProcessConfigControllerTest {
    public static final String AUTOMATIC_PROCESS_CONFIGS_PATH = "/v1/automatic-process-configs";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AutomaticProcessConfigService automaticProcessConfigService;

    @Test
    void updateConfigUsesUserIdHeader() throws Exception {
        UUID configUuid = UUID.randomUUID();
        when(automaticProcessConfigService.update(configUuid, ProcessType.LOADFLOW, "admin")).thenReturn(Optional.of(configUuid));

        mockMvc.perform(put(AUTOMATIC_PROCESS_CONFIGS_PATH + "/" + ProcessType.LOADFLOW)
                .header(MonitorApi.HEADER_USER_ID, "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configUuid)))
            .andExpect(status().isOk());

        verify(automaticProcessConfigService).update(configUuid, ProcessType.LOADFLOW, "admin");
    }

    @Test
    void updateConfigReturnsNotFoundWhenProcessConfigDoesNotExist() throws Exception {
        UUID configUuid = UUID.randomUUID();
        when(automaticProcessConfigService.update(configUuid, ProcessType.LOADFLOW, "admin")).thenReturn(Optional.empty());

        mockMvc.perform(put(AUTOMATIC_PROCESS_CONFIGS_PATH + "/" + ProcessType.LOADFLOW)
                .header(MonitorApi.HEADER_USER_ID, "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configUuid)))
            .andExpect(status().isNotFound());
    }

    @Test
    void getActiveConfigsReturnsConfigNames() throws Exception {
        UUID configUuid = UUID.randomUUID();
        AutomaticProcessConfigInfos infos = new AutomaticProcessConfigInfos(configUuid, "my config", ProcessType.SECURITY_ANALYSIS, null, null);
        when(automaticProcessConfigService.getActives()).thenReturn(List.of(infos));

        mockMvc.perform(get(AUTOMATIC_PROCESS_CONFIGS_PATH))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(List.of(infos))));
    }

    @Test
    void getHistoryReturnsConfigNames() throws Exception {
        UUID configUuid = UUID.randomUUID();
        AutomaticProcessConfigInfos infos = new AutomaticProcessConfigInfos(configUuid, "my config", ProcessType.LOADFLOW, Instant.parse("2026-01-01T00:00:00Z"), "admin");
        when(automaticProcessConfigService.getHistory()).thenReturn(List.of(infos));

        mockMvc.perform(get(AUTOMATIC_PROCESS_CONFIGS_PATH + "/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].processConfigName").value("my config"));
    }
}
