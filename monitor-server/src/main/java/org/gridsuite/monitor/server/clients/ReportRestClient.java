/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.gridsuite.monitor.server.dto.report.ReportInfos;
import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ReportRestClient {
    private static final String REPORT_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public ReportRestClient(@Value("${gridsuite.services.report-server.base-uri:http://report-server/}") String reportServerBaseUri,
                            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl(reportServerBaseUri + DELIMITER + REPORT_API_VERSION + DELIMITER + "reports")
            .build();
    }

    public ReportPage getReport(UUID reportId) {
        return restClient.get()
            .uri("/{reportId}/logs", reportId)
            .retrieve()
            .body(new ParameterizedTypeReference<>() { });
    }

    public void deleteReport(UUID reportId) {
        restClient.delete()
            .uri("/{reportId}", reportId)
            .retrieve()
            .toBodilessEntity();
    }

    public void sendReport(ReportInfos reportInfos) {
        Objects.requireNonNull(reportInfos);

        restClient.put()
            .uri("/{reportUuid}", reportInfos.reportUuid())
            .contentType(MediaType.APPLICATION_JSON)
            .body(reportInfos.reportNode())
            .retrieve()
            .toBodilessEntity();
    }
}
