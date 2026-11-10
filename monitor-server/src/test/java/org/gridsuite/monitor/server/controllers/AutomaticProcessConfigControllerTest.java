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
import org.gridsuite.monitor.server.services.automaticprocessconfig.AutomaticProcessConfigTxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    private AutomaticProcessConfigTxService configService;

    @Test
    void updateConfigUsesUserIdHeader() throws Exception {
        UUID configUuid = UUID.randomUUID();
        when(configService.update(configUuid, ProcessType.LOADFLOW, "admin")).thenReturn(Optional.of(configUuid));

        mockMvc.perform(put(AUTOMATIC_PROCESS_CONFIGS_PATH + "/" + ProcessType.LOADFLOW)
                .header(MonitorController.HEADER_USER_ID, "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configUuid)))
            .andExpect(status().isOk());

        verify(configService).update(configUuid, ProcessType.LOADFLOW, "admin");
    }

    @Test
    void updateConfigReturnsNotFoundWhenProcessConfigDoesNotExist() throws Exception {
        UUID configUuid = UUID.randomUUID();
        when(configService.update(configUuid, ProcessType.LOADFLOW, "admin")).thenReturn(Optional.empty());

        mockMvc.perform(put(AUTOMATIC_PROCESS_CONFIGS_PATH + "/" + ProcessType.LOADFLOW)
                .header(MonitorController.HEADER_USER_ID, "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configUuid)))
            .andExpect(status().isNotFound());
    }

    @Test
    void getConfigReturnsStoredConfigUuid() throws Exception {
        UUID configUuid = UUID.randomUUID();
        when(configService.get(ProcessType.SECURITY_ANALYSIS)).thenReturn(Optional.of(configUuid));

        mockMvc.perform(get(AUTOMATIC_PROCESS_CONFIGS_PATH)
                .param("processType", ProcessType.SECURITY_ANALYSIS.name()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(configUuid)));
    }

    @Test
    void getConfigReturnsNotFoundWhenNoConfigIsStored() throws Exception {
        when(configService.get(ProcessType.LOADFLOW)).thenReturn(Optional.empty());

        mockMvc.perform(get(AUTOMATIC_PROCESS_CONFIGS_PATH)
                .param("processType", ProcessType.LOADFLOW.name()))
            .andExpect(status().isNotFound());
    }
}
