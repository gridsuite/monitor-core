/**
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

    public void deleteResult(UUID resultUuid) {
        restClient.delete()
            .uri(uriBuilder -> uriBuilder
                .path("/results")
                .queryParam("resultsUuids", resultUuid)
                .build())
            .retrieve()
            .toBodilessEntity();
    }

    /**
     * Runs a security analysis on a network stored in S3.
     *
     * @param caseS3Key the S3 key of the network
     * @param securityAnalysisParametersUuid the SA parameters UUID
     * @param loadflowParametersUuid the load flow parameters UUID
     * @return the result UUID
     */
    public UUID run(String caseS3Key, UUID securityAnalysisParametersUuid, UUID loadflowParametersUuid) {
        // TODO: call security-analysis-server's run endpoint with S3 reference
        // Expected API: POST /run
        //   Body: { "caseS3Key": "...", "securityAnalysisParametersUuid": "...", "loadflowParametersUuid": "..." }
        //   Returns: result UUID
        // Then poll for completion or wait for callback
        throw new UnsupportedOperationException("Not yet implemented: security-analysis-server run API");
    }
}
