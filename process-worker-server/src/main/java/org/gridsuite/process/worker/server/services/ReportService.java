/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import lombok.Setter;
import org.gridsuite.process.worker.server.dto.ReportInfos;
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

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ReportService {

    static final String REPORT_API_VERSION = "v1";
    private static final String DELIMITER = "/";
    @Setter
    private String reportServerBaseUri;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public ReportService(ObjectMapper objectMapper,
                         @Value("${gridsuite.services.report-server.base-uri:http://report-server/}") String reportServerBaseUri,
                         RestTemplateBuilder restTemplateBuilder) {
        this.reportServerBaseUri = reportServerBaseUri;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder.build();
    }

    private String getReportServerURI() {
        return this.reportServerBaseUri + DELIMITER + REPORT_API_VERSION + DELIMITER + "reports" + DELIMITER;
    }

    public void sendReport(ReportInfos reportInfos) {
        Objects.requireNonNull(reportInfos);

        var path = UriComponentsBuilder.fromPath("{reportUuid}")
            .buildAndExpand(reportInfos.reportUuid())
            .toUriString();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String str = objectMapper.writeValueAsString(reportInfos.reportNode());
            restTemplate.exchange(getReportServerURI() + path, HttpMethod.PUT, new HttpEntity<>(str, headers), ReportNode.class);
        } catch (JsonProcessingException error) {
            throw new PowsyblException("Error sending report", error);
        }
    }
}
