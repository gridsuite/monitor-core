/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.powsybl.commons.report.ReportNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ReportRestClient {
    static final String REPORT_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public ReportRestClient(@Value("${gridsuite.services.report-server.base-uri:http://report-server/}") String reportServerBaseUri,
                            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl(reportServerBaseUri + DELIMITER + REPORT_API_VERSION + DELIMITER + "reports")
            .build();
    }

    public void sendReport(UUID reportId, ReportNode reportNode) {
        Objects.requireNonNull(reportNode);

        restClient.put()
            .uri("/{reportUuid}", reportId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(reportNode)
            .retrieve()
            .toBodilessEntity();
    }
}
