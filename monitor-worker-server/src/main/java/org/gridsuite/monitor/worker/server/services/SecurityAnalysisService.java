/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.security.SecurityAnalysisResult;
import lombok.Setter;
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
public class SecurityAnalysisService {
    static final String SA_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    @Setter
    private String securityAnalysisServerBaseUri;

    private String getSecurityAnalysisServerBaseUri() {
        return this.securityAnalysisServerBaseUri + DELIMITER + SA_API_VERSION + DELIMITER;
    }

    public SecurityAnalysisService(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.security-analysis-server.base-uri:http://security-analysis-server/}") String securityAnalysisServerBaseUri) {
        this.securityAnalysisServerBaseUri = securityAnalysisServerBaseUri;
        this.restClient = restClientBuilder.baseUrl(getSecurityAnalysisServerBaseUri()).build();
    }

    public void saveResult(UUID resultUuid, SecurityAnalysisResult result) {
        Objects.requireNonNull(result);

        restClient.post()
            .uri("/results/{resultUuid}", resultUuid)
            .body(result)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity();
    }
}
