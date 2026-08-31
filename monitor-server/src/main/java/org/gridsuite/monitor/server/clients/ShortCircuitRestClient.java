/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Service
public class ShortCircuitRestClient {
    static final String SC_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public ShortCircuitRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.shortcircuit-server.base-uri:http://shortcircuit-server/}") String shortCircuitServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(shortCircuitServerBaseUri + DELIMITER + SC_API_VERSION)
            .build();
    }

    public String getResult(UUID resultUuid) {
        return restClient.get()
            .uri("/results/{resultUuid}", resultUuid)
            .retrieve()
            .body(String.class);
    }

    public void deleteResult(UUID resultUuid) {
        restClient.delete()
            .uri(uriBuilder -> uriBuilder
                .path("/results")
                .queryParam("resultsUuids", resultUuid)
                .build())
            .retrieve()
            .toBodilessEntity();
    }
}
