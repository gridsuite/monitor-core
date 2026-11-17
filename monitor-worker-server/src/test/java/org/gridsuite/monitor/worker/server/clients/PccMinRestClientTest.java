/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.powsybl.shortcircuit.FaultResult;
import org.gridsuite.monitor.worker.server.config.MonitorWorkerConfig;
import org.gridsuite.scpmin.provider.dto.ScpMinResult;
import org.gridsuite.scpmin.provider.dto.SingleScpMinResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(PccMinRestClient.class)
@ContextConfiguration(classes = {MonitorWorkerConfig.class, PccMinRestClient.class})
class PccMinRestClientTest {

    private static final UUID RESULT_UUID = UUID.randomUUID();

    @Autowired
    private PccMinRestClient pccMinRestClient;

    @Autowired
    private MockRestServiceServer server;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void saveResult() {
        ScpMinResult result = new ScpMinResult(List.of(
            new SingleScpMinResult("vlId0_0", 1.0, 2.0, "equipment", 3.0, 4.0)), FaultResult.Status.SUCCESS);
        server.expect(requestTo("http://pcc-min-server/v1/results/" + RESULT_UUID))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                {
                  "results": [{
                    "busId": "vlId0_0",
                    "pccMinTri": 1.0,
                    "iccMinTri": 2.0,
                    "limitingEquipment": "equipment",
                    "r": 3.0,
                    "x": 4.0
                  }],
                  "status": "SUCCESS"
                }
                """))
            .andRespond(withSuccess("\"" + RESULT_UUID + "\"", MediaType.APPLICATION_JSON));

        pccMinRestClient.saveResult(RESULT_UUID, result);
    }

    @Test
    void saveResultFailurePropagates() {
        ScpMinResult result = new ScpMinResult(List.of(), FaultResult.Status.SUCCESS);
        server.expect(requestTo("http://pcc-min-server/v1/results/" + RESULT_UUID))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        assertThatThrownBy(() -> pccMinRestClient.saveResult(RESULT_UUID, result))
            .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
