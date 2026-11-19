/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import org.gridsuite.powsybl.extras.scpmin.api.dto.ScpMinResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class PccMinRestClient {

    private final RestClient restClient;

    public PccMinRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.pccmin-server.base-uri:http://pcc-min-server/}") String pccMinServerBaseUri) {
        this.restClient = restClientBuilder.baseUrl(pccMinServerBaseUri + "/v1").build();
    }

    public void saveResult(UUID resultUuid, ScpMinResult result) {
        Objects.requireNonNull(resultUuid);
        Objects.requireNonNull(result);
        restClient.post()
            .uri("/results/{resultUuid}", resultUuid)
            .contentType(MediaType.APPLICATION_JSON)
            .body(result)
            .retrieve()
            .toBodilessEntity();
    }
}
