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

import java.util.List;
import java.util.UUID;

/**
 * REST client for the network-modification-server.
 * Used to apply modifications to a network stored in S3.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class NetworkModificationRestClient {
    private static final String NETWORK_MODIFICATION_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public NetworkModificationRestClient(
        @Value("${gridsuite.services.network-modification-server.base-uri:http://network-modification-server/}") String networkModificationServerBaseUri,
        RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl(networkModificationServerBaseUri + DELIMITER + NETWORK_MODIFICATION_SERVER_API_VERSION)
            .build();
    }

    /**
     * Applies modifications to a network stored in S3.
     *
     * @param caseS3Key the S3 key of the network to modify
     * @param modificationUuids the list of modification UUIDs to apply
     * @return the S3 key of the modified network (may be same or new key)
     */
    public String applyModifications(String caseS3Key, List<UUID> modificationUuids) {
        // TODO: call network-modification-server API to apply modifications on a network in S3
        // Expected API: POST /network-modifications/apply
        //   Body: { "caseS3Key": "...", "modificationUuids": [...] }
        //   Returns: S3 key of the modified network
        throw new UnsupportedOperationException("Not yet implemented: network-modification-server apply API");
    }
}
