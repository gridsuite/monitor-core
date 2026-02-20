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
public class NetworkModificationRestService {
    private static final String NETWORK_MODIFICATION_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestTemplate networkModificationServerRest;
    private final String networkModificationServerBaseUri;

    public NetworkModificationRestService(@Value("${gridsuite.services.network-modification-server.base-uri:http://network-modification-server/}") String networkModificationServerBaseUri,
                                          RestTemplateBuilder restTemplateBuilder) {
        this.networkModificationServerRest = restTemplateBuilder.build();
        this.networkModificationServerBaseUri = networkModificationServerBaseUri;
    }

    public void applyModifications(UUID caseUuid, UUID executionId, List<UUID> modificationUuids) {
        var uriComponentsBuilder = UriComponentsBuilder.fromPath(DELIMITER + NETWORK_MODIFICATION_SERVER_API_VERSION + DELIMITER + "cases/{caseUuid}/network-composite-modifications");
        var path = uriComponentsBuilder
            .queryParam("executionUuid", executionId)
            .queryParam("uuids", modificationUuids)
            .buildAndExpand(caseUuid)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity httpEntity = new HttpEntity<>(null, headers);

        networkModificationServerRest.exchange(networkModificationServerBaseUri + path, HttpMethod.POST, httpEntity, Void.class);
    }
}
