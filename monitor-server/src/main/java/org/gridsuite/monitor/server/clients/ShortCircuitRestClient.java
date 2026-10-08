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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Service
public class ShortCircuitRestClient {
    static final String SC_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;
    private final String shortCircuitServerBaseUri;

    public ShortCircuitRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.shortcircuit-server.base-uri:http://shortcircuit-server/}") String shortCircuitServerBaseUri) {
        this.shortCircuitServerBaseUri = shortCircuitServerBaseUri;
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

    public String getResult(UUID resultUuid, ResultQueryParams queryParams) {
        if (queryParams == null || queryParams.page() == null && queryParams.size() == null
            && queryParams.sort().isEmpty() && queryParams.filters() == null) {
            return getResult(resultUuid);
        }
        StringBuilder uri = new StringBuilder(shortCircuitServerBaseUri.replaceAll("/+$", ""))
            .append(DELIMITER).append(SC_API_VERSION)
            .append("/results/").append(resultUuid)
            .append("?type=").append(queryParams.shortCircuitResultType())
            .append("&paged=true");
        if (queryParams.page() != null) {
            uri.append("&page=").append(queryParams.page());
        }
        if (queryParams.size() != null) {
            uri.append("&size=").append(queryParams.size());
        }
        queryParams.sort().forEach(sort -> uri.append("&sort=").append(sort));
        if (queryParams.filters() != null) {
            uri.append("&filters=").append(URLEncoder.encode(queryParams.filters(), StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return restClient.get()
            .uri(URI.create(uri.toString()))
            .retrieve()
            .body(String.class);
    }

    public byte[] exportResult(UUID resultUuid, ResultQueryParams queryParams, String csvTranslations) {
        StringBuilder uri = new StringBuilder(shortCircuitServerBaseUri.replaceAll("/+$", ""))
            .append(DELIMITER).append(SC_API_VERSION)
            .append("/results/").append(resultUuid)
            .append("/csv?type=").append(queryParams.shortCircuitResultType());
        queryParams.sort().forEach(sort -> uri.append("&sort=").append(sort));
        if (queryParams.filters() != null) {
            uri.append("&filters=").append(URLEncoder.encode(queryParams.filters(), StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return restClient.post()
            .uri(URI.create(uri.toString()))
            .body(csvTranslations)
            .retrieve()
            .body(byte[].class);
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
