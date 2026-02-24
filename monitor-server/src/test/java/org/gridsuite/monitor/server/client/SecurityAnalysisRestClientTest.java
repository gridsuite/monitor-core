/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.client;

import org.gridsuite.monitor.server.client.SecurityAnalysisRestClient;
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
import org.springframework.web.client.RestClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@RestClientTest(SecurityAnalysisRestClient.class)
@ContextConfiguration(classes = { SecurityAnalysisRestClient.class })
class SecurityAnalysisRestClientTest {

    private static final UUID RESULT_UUID = UUID.randomUUID();
    private static final String RESULT_BODY = "{\"status\":\"OK\"}";

    @Autowired
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    @Autowired
    private MockRestServiceServer server;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getResult() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://security-analysis-server/v1/results/" + RESULT_UUID + "/nmk-contingencies-result"
            ))
            .andRespond(MockRestResponseCreators.withSuccess(
                RESULT_BODY,
                MediaType.APPLICATION_JSON
            ));

        String result = securityAnalysisRestClient.getResult(RESULT_UUID);

        assertThat(result).isEqualTo(RESULT_BODY);
    }

    @Test
    void getResultFailed() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo(
                "http://security-analysis-server/v1/results/" + RESULT_UUID + "/nmk-contingencies-result"
            ))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> securityAnalysisRestClient.getResult(RESULT_UUID))
            .isInstanceOf(RestClientException.class);
    }

    @Test
    void deleteResult() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andExpect(MockRestRequestMatchers.requestTo("http://security-analysis-server/v1/results?resultsUuids=" + RESULT_UUID))
            .andRespond(MockRestResponseCreators.withSuccess());

        assertThatNoException().isThrownBy(() -> securityAnalysisRestClient.deleteResult(RESULT_UUID));
    }

    @Test
    void deleteResultFailed() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andExpect(MockRestRequestMatchers.requestTo("http://security-analysis-server/v1/results?resultsUuids=" + RESULT_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> securityAnalysisRestClient.deleteResult(RESULT_UUID))
            .isInstanceOf(RestClientException.class);
    }
}
