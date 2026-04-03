/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * REST client for the case-server. Used to export network cases to S3.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class CaseServerRestClient {
    private static final String CASE_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public CaseServerRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.case-server.base-uri:http://case-server/}") String caseServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(caseServerBaseUri + DELIMITER + CASE_API_VERSION)
            .build();
    }

    /**
     * Exports a case to S3 and returns the S3 key where the network is stored.
     *
     * @param caseUuid the UUID of the case to export
     * @return the S3 key of the exported network
     */
    public String exportCaseToS3(UUID caseUuid) {
        // TODO: call case-server API to export network to S3
        // Expected API: POST /cases/{caseUuid}/export-to-s3
        // Returns: S3 key where the network was stored
        throw new UnsupportedOperationException("Not yet implemented: case-server export-to-s3 API");
    }
}
