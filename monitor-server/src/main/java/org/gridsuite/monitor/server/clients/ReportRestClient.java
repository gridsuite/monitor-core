/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ReportRestClient {
    private static final String REPORT_API_VERSION = "v1";

    private static final String DELIMITER = "/";

    private final String reportServerBaseUri;

    private final RestTemplate restTemplate;

    public ReportRestClient(@Value("${gridsuite.services.report-server.base-uri:http://report-server/}") String reportServerBaseUri,
                            RestTemplateBuilder restTemplateBuilder) {
        this.reportServerBaseUri = reportServerBaseUri;
        this.restTemplate = restTemplateBuilder.build();
    }

    private String getReportServerURI() {
        return this.reportServerBaseUri + DELIMITER + REPORT_API_VERSION + DELIMITER + "reports" + DELIMITER;
    }

    public ReportPage getReport(UUID reportId) {
        var uriBuilder = UriComponentsBuilder.fromPath("{id}/logs");

        var path = uriBuilder.buildAndExpand(reportId).toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(this.getReportServerURI() + path, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<ReportPage>() { }).getBody();
    }

    public void deleteReport(UUID reportId) {
        var path = UriComponentsBuilder.fromPath("{id}")
            .buildAndExpand(reportId)
            .toUriString();

        restTemplate.delete(this.getReportServerURI() + path);
    }
}
