/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.actions.dto.contingency.PersistentContingencyList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(ActionsRestService.class)
class ActionsRestServiceTest {

    @Autowired
    private ActionsRestService actionsRestService;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID CONTINGENCY_1_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CONTINGENCY_2_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CONTINGENCY_ERROR_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getPersistentContingencyLists() {
        List<UUID> requestUuids = List.of(CONTINGENCY_1_UUID, CONTINGENCY_2_UUID);

        // TODO : to test with result not empty but jackson deserialization pb ...
        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://actions-server/v1/contingency-lists"))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockRestRequestMatchers.jsonPath("$[0]").value(CONTINGENCY_1_UUID.toString()))
            .andExpect(MockRestRequestMatchers.jsonPath("$[1]").value(CONTINGENCY_2_UUID.toString()))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body("[]"));

        List<PersistentContingencyList> result = actionsRestService.getPersistentContingencyLists(requestUuids);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getPersistentContingencyListsNotFound() {
        List<UUID> requestUuids = List.of(CONTINGENCY_ERROR_UUID);

        try {
            server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.requestTo("http://actions-server/v1/contingency-lists"))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(requestUuids)))
                .andRespond(MockRestResponseCreators.withServerError());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        assertThatThrownBy(() -> actionsRestService.getPersistentContingencyLists(requestUuids))
            .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    @Test
    void getEmptyContingencies() {
        List<PersistentContingencyList> result = actionsRestService.getPersistentContingencyLists(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void getPersistentContingencyListsWithEmptyResponse() {
        List<UUID> requestUuids = List.of(CONTINGENCY_1_UUID);

        try {
            server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.requestTo("http://actions-server/v1/contingency-lists"))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(requestUuids)))
                .andRespond(MockRestResponseCreators.withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(List.of())));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<PersistentContingencyList> result = actionsRestService.getPersistentContingencyLists(requestUuids);

        assertThat(result).isEmpty();
    }
}
