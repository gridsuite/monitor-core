/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(LoadFlowRestClient.class)
class LoadFlowRestClientTest {
    @Autowired
    private LoadFlowRestClient loadFlowRestClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID PARAMETERS_UUID = UUID.randomUUID();
    private static final UUID PARAMETERS_ERROR_UUID = UUID.randomUUID();

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getParameters() throws JsonProcessingException {
        LoadFlowParametersInfos expectedParameters = LoadFlowParametersInfos.builder()
            .provider("OpenLoadFlow")
            .build();

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://load-flow-server/v1/parameters/" + PARAMETERS_UUID))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(expectedParameters)));

        LoadFlowParametersInfos result = loadFlowRestClient.getParameters(PARAMETERS_UUID);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void getParametersNotFound() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://load-flow-server/v1/parameters/" + PARAMETERS_ERROR_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> loadFlowRestClient.getParameters(PARAMETERS_ERROR_UUID))
            .isInstanceOf(PowsyblException.class)
            .hasMessageContaining("Error retrieving loadflow parameters");
    }
}
