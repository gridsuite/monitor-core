/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@RestClientTest(ShortCircuitRestClient.class)
@ContextConfiguration(classes = {ShortCircuitRestClient.class})
class ShortCircuitRestClientTest {

    @Autowired
    private ShortCircuitRestClient shortCircuitRestClient;

    @Autowired
    private MockRestServiceServer server;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getResultSuccess() {
        UUID resultUuid = UUID.randomUUID();
        String expectedResult = "result";

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://shortcircuit-server/v1/results/" + resultUuid))
            .andRespond(MockRestResponseCreators.withSuccess(expectedResult, MediaType.TEXT_PLAIN));

        String result = shortCircuitRestClient.getResult(resultUuid);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getResultFailed() {
        UUID resultUuid = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://shortcircuit-server/v1/results/" + resultUuid))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> shortCircuitRestClient.getResult(resultUuid))
            .isInstanceOf(RestClientException.class);
    }

    @Test
    void deleteResultSuccess() {
        UUID resultUuid = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andExpect(MockRestRequestMatchers.requestTo("http://shortcircuit-server/v1/results?resultsUuids=" + resultUuid))
            .andRespond(MockRestResponseCreators.withSuccess());

        shortCircuitRestClient.deleteResult(resultUuid);
    }
}
