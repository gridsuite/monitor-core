/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import org.gridsuite.monitor.worker.server.config.MonitorWorkerConfig;
import org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit.ShortCircuitParametersInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit.ShortCircuitPredefinedConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@RestClientTest(ShortCircuitRestClient.class)
@ContextConfiguration(classes = {MonitorWorkerConfig.class, ShortCircuitRestClient.class})
class ShortCircuitRestClientTest {

    private static final UUID RESULT_UUID = UUID.randomUUID();
    private static final UUID PARAMETERS_UUID = UUID.randomUUID();
    private static final UUID PARAMETERS_ERROR_UUID = UUID.randomUUID();

    @Autowired
    private ShortCircuitRestClient shortCircuitRestClient;

    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getParameters() throws JsonProcessingException {
        ShortCircuitParametersInfos expectedParameters = ShortCircuitParametersInfos.builder()
            .provider("ShortCircuit-provider")
            .predefinedParameters(ShortCircuitPredefinedConfiguration.ICC_MAX_WITH_CEI909)
            .commonParameters(new ShortCircuitParameters())
            .specificParametersPerProvider(Map.of("ShortCircuit-provider", Map.of("nodeCluster", "bus1")))
            .build();

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://shortcircuit-server/v1/parameters/" + PARAMETERS_UUID))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(expectedParameters)));

        ShortCircuitParametersInfos result = shortCircuitRestClient.getParameters(PARAMETERS_UUID);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void getParametersNotFound() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://shortcircuit-server/v1/parameters/" + PARAMETERS_ERROR_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> shortCircuitRestClient.getParameters(PARAMETERS_ERROR_UUID))
            .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    @Test
    void saveResultSuccess() throws JsonProcessingException {
        ShortCircuitAnalysisResult result = mock(ShortCircuitAnalysisResult.class);

        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://shortcircuit-server/v1/results/" + RESULT_UUID))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(result)))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(RESULT_UUID)));

        assertThatNoException().isThrownBy(() -> shortCircuitRestClient.saveResult(RESULT_UUID, result));
    }

    @Test
    void saveResultFailed() {
        ShortCircuitAnalysisResult result = mock(ShortCircuitAnalysisResult.class);
        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://shortcircuit-server/v1/results/" + RESULT_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> shortCircuitRestClient.saveResult(RESULT_UUID, result)).isInstanceOf(RestClientException.class);
    }
}
