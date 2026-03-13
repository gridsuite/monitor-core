/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.actions.dto.EquipmentTypesByFilter;
import org.gridsuite.actions.dto.FilterAttributes;
import org.gridsuite.actions.dto.contingency.AbstractContingencyList;
import org.gridsuite.actions.dto.contingency.FilterBasedContingencyList;
import org.gridsuite.actions.dto.contingency.IdBasedContingencyList;
import org.gridsuite.filter.utils.EquipmentType;
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

import java.time.Instant;
import java.util.List;
import java.util.Set;
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
    void getPersistentContingencyLists() throws JsonProcessingException {
        List<UUID> requestUuids = List.of(CONTINGENCY_1_UUID, CONTINGENCY_2_UUID);

        IdBasedContingencyList idBasedContingencyList = new IdBasedContingencyList(CONTINGENCY_1_UUID, Instant.now(), null);
        FilterBasedContingencyList filterBasedContingencyList = new FilterBasedContingencyList(CONTINGENCY_2_UUID, Instant.now(),
            List.of(new FilterAttributes(UUID.randomUUID(), EquipmentType.GENERATOR, "gen1")),
            List.of(new EquipmentTypesByFilter(UUID.randomUUID(), Set.of(IdentifiableType.GENERATOR))));
        List<AbstractContingencyList> abstractContingencyLists = List.of(idBasedContingencyList, filterBasedContingencyList);
        String jsonResponse = objectMapper.writeValueAsString(abstractContingencyLists);

        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://actions-server/v1/contingency-lists"))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(requestUuids)))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse));

        List<AbstractContingencyList> result = actionsRestService.getPersistentContingencyLists(requestUuids);
        assertThat(result).isNotNull();

        assertThat(result).usingRecursiveComparison().isEqualTo(objectMapper.readValue(jsonResponse, new TypeReference<List<AbstractContingencyList>>() { }));
    }

    @Test
    void getPersistentContingencyListsNotFound() throws JsonProcessingException {
        List<UUID> requestUuids = List.of(CONTINGENCY_ERROR_UUID);

        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.requestTo("http://actions-server/v1/contingency-lists"))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(requestUuids)))
                .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> actionsRestService.getPersistentContingencyLists(requestUuids))
            .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    @Test
    void getEmptyContingencies() {
        List<AbstractContingencyList> result = actionsRestService.getPersistentContingencyLists(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void getPersistentContingencyListsWithEmptyResponse() throws JsonProcessingException {
        List<UUID> requestUuids = List.of(CONTINGENCY_1_UUID);

        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://actions-server/v1/contingency-lists"))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(requestUuids)))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(List.of())));

        List<AbstractContingencyList> result = actionsRestService.getPersistentContingencyLists(requestUuids);

        assertThat(result).isEmpty();
    }
}
