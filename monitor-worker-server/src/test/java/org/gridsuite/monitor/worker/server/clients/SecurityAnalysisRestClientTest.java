/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.security.SecurityAnalysisResult;
import org.gridsuite.monitor.worker.server.config.MonitorWorkerConfig;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisParametersValues;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@RestClientTest(SecurityAnalysisRestClient.class)
@ContextConfiguration(classes = {MonitorWorkerConfig.class, SecurityAnalysisRestClient.class})
class SecurityAnalysisRestClientTest {

    private static final UUID RESULT_UUID = UUID.randomUUID();
    private static final UUID PARAMETERS_UUID = UUID.randomUUID();
    private static final UUID PARAMETERS_ERROR_UUID = UUID.randomUUID();

    @Autowired
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void saveResult() throws JsonProcessingException {
        SecurityAnalysisResult result = SecurityAnalysisResult.empty();

        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://security-analysis-server/v1/results/" + RESULT_UUID))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(result)))
            .andRespond(MockRestResponseCreators.withSuccess());

        assertThatNoException().isThrownBy(() -> securityAnalysisRestClient.saveResult(RESULT_UUID, result));
    }

    @Test
    void saveResultFailed() {
        SecurityAnalysisResult result = SecurityAnalysisResult.empty();
        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://security-analysis-server/v1/results/" + RESULT_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> securityAnalysisRestClient.saveResult(RESULT_UUID, result)).isInstanceOf(RestClientException.class);
    }

    @Test
    void getParameters() throws JsonProcessingException {
        SecurityAnalysisParametersValues expectedParameters = SecurityAnalysisParametersValues.builder()
            .flowProportionalThreshold(0.1)
            .lowVoltageProportionalThreshold(0.05)
            .highVoltageProportionalThreshold(0.05)
            .lowVoltageAbsoluteThreshold(10.0)
            .highVoltageAbsoluteThreshold(10.0)
            .build();

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://security-analysis-server/v1/parameters/" + PARAMETERS_UUID))
            .andExpect(MockRestRequestMatchers.header("userId", "user1"))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(expectedParameters)));

        SecurityAnalysisParametersValues result = securityAnalysisRestClient.getParameters(PARAMETERS_UUID);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedParameters);
    }

    @Test
    void getParametersNotFound() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://security-analysis-server/v1/parameters/" + PARAMETERS_ERROR_UUID))
            .andExpect(MockRestRequestMatchers.header("userId", "user1"))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> securityAnalysisRestClient.getParameters(PARAMETERS_ERROR_UUID))
            .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
