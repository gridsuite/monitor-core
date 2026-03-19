/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.powsybl.security.SecurityAnalysisResult;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisParametersValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;
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

    public void saveResult(UUID resultUuid, SecurityAnalysisResult result) {
        Objects.requireNonNull(result);
        restClient.post()
            .uri("/results/{resultUuid}", resultUuid)
            .contentType(MediaType.APPLICATION_JSON)
            .body(result)
            .retrieve()
            .toBodilessEntity();
    }

    public SecurityAnalysisParametersValues getParameters(UUID securityAnalysisParametersUuid) {
        return restClient.get()
            .uri("/parameters/{securityAnalysisParametersUuid}",
                securityAnalysisParametersUuid)
            .header("userId", "user1")
            .retrieve()
            .body(SecurityAnalysisParametersValues.class);
    }
}
