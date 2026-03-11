/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.server.dto.ReportPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ReportService {
    private static final String REPORT_API_VERSION = "v1";

    private static final String DELIMITER = "/";

    private final String reportServerBaseUri;

    private final RestClient restClient;

    public ReportService(@Value("${gridsuite.services.report-server.base-uri:http://report-server/}") String reportServerBaseUri,
                         RestClient.Builder restClientBuilder) {
        this.reportServerBaseUri = reportServerBaseUri;
        this.restClient = restClientBuilder.baseUrl(getReportServerURI()).build();
    }

    private String getReportServerURI() {
        return this.reportServerBaseUri + DELIMITER + REPORT_API_VERSION + DELIMITER + "reports" + DELIMITER;
    }

    public ReportPage getReport(UUID reportId) {
        return restClient.get()
            .uri("{id}/logs", reportId)
            .retrieve()
            .body(new ParameterizedTypeReference<>() { });
    }

    public void deleteReport(UUID reportId) {
        restClient.delete()
            .uri("{id}", reportId)
            .retrieve()
            .toBodilessEntity();
    }
}
