/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.powsybl.shortcircuit.FaultResult;
import org.gridsuite.monitor.worker.server.config.MonitorWorkerConfig;
import org.gridsuite.powsybl.extras.scpmin.api.dto.ScpMinResult;
import org.gridsuite.powsybl.extras.scpmin.api.dto.SingleScpMinResult;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RestClientTest(PccMinRestClient.class)
@ContextConfiguration(classes = {MonitorWorkerConfig.class, PccMinRestClient.class})
class PccMinRestClientTest {

    private static final UUID RESULT_UUID = UUID.randomUUID();
    private static final ScpMinResult RESULT = new ScpMinResult(
        List.of(new SingleScpMinResult("bus1", 100.0, 10.0, "line1", 0.1, 0.2)),
        FaultResult.Status.SUCCESS);

    @Autowired
    private PccMinRestClient pccMinRestClient;

    @Autowired
    private MockRestServiceServer server;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void saveResultSuccess() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://pcc-min-server/v1/results/" + RESULT_UUID))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockRestRequestMatchers.content().json("""
                {
                    "results": [{
                        "busId": "bus1",
                        "pccMinTri": 100.0,
                        "iccMinTri": 10.0,
                        "limitingEquipment": "line1",
                        "r": 0.1,
                        "x": 0.2
                    }],
                    "status": "SUCCESS"
                }
                """))
            .andRespond(MockRestResponseCreators.withSuccess());

        assertThatNoException().isThrownBy(() -> pccMinRestClient.saveResult(RESULT_UUID, RESULT));
    }

    @Test
    void saveResultFailed() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://pcc-min-server/v1/results/" + RESULT_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> pccMinRestClient.saveResult(RESULT_UUID, RESULT))
            .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
