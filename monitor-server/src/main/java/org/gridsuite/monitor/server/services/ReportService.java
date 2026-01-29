/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.server.dto.Report;
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
public class ReportService {
    private static final String REPORT_API_VERSION = "v1";

    private static final String DELIMITER = "/";

    private final String reportServerBaseUri;

    private final RestTemplate restTemplate;

    public ReportService(@Value("${gridsuite.services.report-server.base-uri:http://report-server/}") String reportServerBaseUri,
                         RestTemplateBuilder restTemplateBuilder) {
        this.reportServerBaseUri = reportServerBaseUri;
        this.restTemplate = restTemplateBuilder.build();
    }

    private String getReportServerURI() {
        return this.reportServerBaseUri + DELIMITER + REPORT_API_VERSION + DELIMITER + "reports" + DELIMITER;
    }

    public Report getReport(UUID reportId) {
        var uriBuilder = UriComponentsBuilder.fromPath("{id}");

        var path = uriBuilder
            .queryParam("withLeaves", Boolean.TRUE)
            .buildAndExpand(reportId).toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(this.getReportServerURI() + path, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<Report>() { }).getBody();
    }
}
