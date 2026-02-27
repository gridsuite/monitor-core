/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.powsybl.security.SecurityAnalysisResult;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Service
public class SecurityAnalysisRestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAnalysisRestClient.class);
    static final String SA_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestTemplate restTemplate;

    @Setter
    private String securityAnalysisServerBaseUri;

    private String getSecurityAnalysisServerBaseUri() {
        return this.securityAnalysisServerBaseUri + DELIMITER + SA_API_VERSION + DELIMITER;
    }

    public SecurityAnalysisRestClient(
        RestTemplateBuilder restTemplateBuilder,
        @Value("${gridsuite.services.security-analysis-server.base-uri:http://security-analysis-server/}") String securityAnalysisServerBaseUri) {
        this.securityAnalysisServerBaseUri = securityAnalysisServerBaseUri;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void saveResult(UUID resultUuid, SecurityAnalysisResult result) {
        Objects.requireNonNull(result);
        LOGGER.info("Saving result {}", resultUuid);

        var path = UriComponentsBuilder.fromPath("/results/{resultUuid}")
            .buildAndExpand(resultUuid)
            .toUriString();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.exchange(getSecurityAnalysisServerBaseUri() + path, HttpMethod.POST, new HttpEntity<>(result, headers), Void.class);
    }
}
