/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.server.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class SecurityAnalysisRestService {
    private static final String SECURITY_ANALYSIS_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestTemplate securityAnalysisServerRest;
    private final String securityAnalysisServerBaseUri;

    public SecurityAnalysisRestService(@Value("${gridsuite.services.security-analysis-server.base-uri:http://security-analysis-server/}") String securityAnalysisServerBaseUri,
                                       RestTemplateBuilder restTemplateBuilder) {
        this.securityAnalysisServerRest = restTemplateBuilder.build();
        this.securityAnalysisServerBaseUri = securityAnalysisServerBaseUri;
    }

    public void runSecurityAnalysis(UUID caseUuid, UUID executionId, List<String> contingencies, UUID parametersUuid) {
        var uriComponentsBuilder = UriComponentsBuilder.fromPath(DELIMITER + SECURITY_ANALYSIS_SERVER_API_VERSION + DELIMITER + "/cases/{caseUuid}/run-and-save");
        var path = uriComponentsBuilder
            .queryParam("executionUuid", executionId)
            .queryParam("contingencyListName", contingencies)
            .queryParam("parametersUuid", parametersUuid)
            .buildAndExpand(caseUuid)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity httpEntity = new HttpEntity<>(null, headers);

        securityAnalysisServerRest.exchange(securityAnalysisServerBaseUri + path, HttpMethod.POST, httpEntity, Void.class);
    }
}
