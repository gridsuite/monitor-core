/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.commons.types.processexecution.ProcessExecution;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.worker.server.config.MonitorWorkerConfig;
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
 * @author Radouane KHOUADRI <redouane.khouadri_externe at rte-france.com>
 */
@RestClientTest(MonitorRestClientTest.class)
@ContextConfiguration(classes = {MonitorWorkerConfig.class, MonitorRestClient.class})
class MonitorRestClientTest {

    private static final UUID EXECUTION_UUID = UUID.randomUUID();

    @Autowired
    private MonitorRestClient monitorRestClient;

    @Autowired
    private MockRestServiceServer server;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void isExecutionCanceledOk() throws JsonProcessingException {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://monitor-server/v1/executions/" + EXECUTION_UUID))
                .andRespond(MockRestResponseCreators.withSuccess()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ObjectMapper().writeValueAsString(ProcessExecution.builder().status(ProcessStatus.CANCELED).build())));
        boolean isExecutionCanceled = monitorRestClient.isExecutionCanceled(EXECUTION_UUID);
        assertThat(isExecutionCanceled).isTrue();
    }

    @Test
    void isExecutionCanceledFailed() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://monitor-server/v1/executions/" + EXECUTION_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> monitorRestClient.isExecutionCanceled(EXECUTION_UUID)).isInstanceOf(RestClientException.class);
    }
}
