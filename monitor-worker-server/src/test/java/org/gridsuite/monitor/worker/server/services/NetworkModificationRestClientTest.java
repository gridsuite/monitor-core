/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.modification.dto.AttributeModification;
import org.gridsuite.modification.dto.LoadModificationInfos;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.dto.OperationType;
import org.gridsuite.monitor.worker.server.client.NetworkModificationRestClient;
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
@RestClientTest(NetworkModificationRestClient.class)
class NetworkModificationRestClientTest {
    @Autowired
    private NetworkModificationRestClient networkModificationRestClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID MODIFICATION_1_UUID = UUID.fromString("11111111-7977-4592-ba19-88027e4254e4");
    private static final UUID MODIFICATION_2_UUID = UUID.fromString("22222222-7977-4592-ba19-88027e4254e4");
    private static final UUID MODIFICATION_ERROR_UUID = UUID.fromString("33333333-7977-4592-ba19-88027e4254e4");

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getModifications() {
        ModificationInfos modificationInfos1 = LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build();
        ModificationInfos modificationInfos2 = LoadModificationInfos.builder().equipmentId("load2").q0(new AttributeModification<>(null, OperationType.UNSET)).build();

        List<ModificationInfos> modificationInfos = List.of(modificationInfos1, modificationInfos2);
        ModificationInfos[] modificationsArray = modificationInfos.toArray(ModificationInfos[]::new);

        try {
            server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://network-modification-server/v1/network-composite-modifications/network-modifications?uuids=" + MODIFICATION_1_UUID + "&uuids=" + MODIFICATION_2_UUID + "&onlyMetadata=false"))
                .andRespond(MockRestResponseCreators.withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(modificationsArray)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ModificationInfos> resultListModifications = networkModificationRestClient.getModifications(List.of(MODIFICATION_1_UUID, MODIFICATION_2_UUID));
        assertThat(resultListModifications).usingRecursiveComparison().isEqualTo(modificationInfos);
    }

    @Test
    void getModificationsNotFound() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://network-modification-server/v1/network-composite-modifications/network-modifications?uuids=" + MODIFICATION_ERROR_UUID + "&onlyMetadata=false"))
            .andRespond(MockRestResponseCreators.withServerError());

        List<UUID> modificationsUuids = List.of(MODIFICATION_ERROR_UUID);
        assertThatThrownBy(() -> networkModificationRestClient.getModifications(modificationsUuids)).isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    @Test
    void getEmptyModifications() {
        List<ModificationInfos> resultListModifications = networkModificationRestClient.getModifications(List.of());
        assertThat(resultListModifications).isEmpty();
    }
}
