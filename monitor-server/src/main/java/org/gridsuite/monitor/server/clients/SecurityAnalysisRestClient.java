/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.gridsuite.monitor.commons.types.result.SecurityAnalysisResultType;
import org.gridsuite.monitor.server.services.result.ResultQueryParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Service
public class SecurityAnalysisRestClient {
    static final String SA_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public SecurityAnalysisRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.security-analysis-server.base-uri:http://security-analysis-server/}") String securityAnalysisServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(securityAnalysisServerBaseUri + DELIMITER + SA_API_VERSION)
            .build();
    }

    public String getResult(UUID resultUuid) {
        return restClient.get()
            .uri("/results/{resultUuid}/nmk-contingencies-result", resultUuid)
            .retrieve()
            .body(String.class);
    }

    public String getResult(UUID resultUuid, ResultQueryParams queryParams) {
        SecurityAnalysisResultType resultType = queryParams == null
            ? SecurityAnalysisResultType.NMK_CONTINGENCIES : queryParams.securityAnalysisResultType();
        String path = switch (resultType) {
            case NMK_LIMIT_VIOLATIONS -> "/results/{resultUuid}/nmk-constraints-result/paged";
            case NMK_CUT_OFF_POWER -> "/results/{resultUuid}/nmk-cut-off-power-result/paged";
            case NMK_CONTINGENCIES -> "/results/{resultUuid}/nmk-contingencies-result/paged";
        };
        return restClient.get()
            .uri(uriBuilder -> {
                uriBuilder.path(path);
                if (queryParams != null && queryParams.page() != null) {
                    uriBuilder.queryParam("page", queryParams.page());
                }
                if (queryParams != null && queryParams.size() != null) {
                    uriBuilder.queryParam("size", queryParams.size());
                }
                if (queryParams != null) {
                    queryParams.sort().forEach(sort -> uriBuilder.queryParam("sort", sort));
                    if (queryParams.filters() != null) {
                        uriBuilder.queryParam("filters", queryParams.filters());
                    }
                }
                return uriBuilder.build(resultUuid);
            })
            .retrieve()
            .body(String.class);
    }

    public byte[] exportResult(UUID resultUuid, ResultQueryParams queryParams, String csvTranslations) {
        String path = switch (queryParams.securityAnalysisResultType()) {
            case NMK_CONTINGENCIES -> "/results/{resultUuid}/nmk-contingencies-result/csv";
            case NMK_LIMIT_VIOLATIONS -> "/results/{resultUuid}/nmk-constraints-result/csv";
            case NMK_CUT_OFF_POWER -> "/results/{resultUuid}/nmk-cut-off-power-result/csv";
        };
        return restClient.post()
            .uri(uriBuilder -> {
                uriBuilder.path(path);
                queryParams.sort().forEach(sort -> uriBuilder.queryParam("sort", sort));
                if (queryParams.filters() != null) {
                    uriBuilder.queryParam("filters", queryParams.filters());
                }
                return uriBuilder.build(resultUuid);
            })
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
