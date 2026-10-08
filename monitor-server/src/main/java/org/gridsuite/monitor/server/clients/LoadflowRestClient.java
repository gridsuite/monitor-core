/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.gridsuite.monitor.server.services.result.ResultQueryParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Service
public class LoadflowRestClient {
    static final String LOADFLOW_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public LoadflowRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.loadflow-server.base-uri:http://loadflow-server/}") String loadflowServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(loadflowServerBaseUri + DELIMITER + LOADFLOW_API_VERSION)
            .build();
    }

    public String getResult(UUID resultUuid) {
        return restClient.get()
            .uri("/results/{resultUuid}", resultUuid)
            .retrieve()
            .body(String.class);
    }

    public String getResult(UUID resultUuid, ResultQueryParams queryParams) {
        return restClient.get()
            .uri(uriBuilder -> {
                uriBuilder.path("/results/{resultUuid}/current-limit-violations");
                addQueryParams(uriBuilder, queryParams);
                return uriBuilder.build(resultUuid);
            })
            .retrieve()
            .body(String.class);
    }

    private void addQueryParams(org.springframework.web.util.UriBuilder uriBuilder, ResultQueryParams queryParams) {
        if (queryParams == null) {
            return;
        }
        queryParams.sort().forEach(sort -> uriBuilder.queryParam("sort", sort));
        if (queryParams.filters() != null) {
            uriBuilder.queryParam("filters", queryParams.filters());
        }
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
