/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.worker.server.services;

import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.modification.dto.ModificationInfos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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

    private final RestClient networkModificationServerRest;

    public NetworkModificationRestService(@Value("${gridsuite.services.network-modification-server.base-uri:http://network-modification-server/}") String networkModificationServerBaseUri,
                                          RestClient.Builder restClientBuilder) {
        this.networkModificationServerRest = restClientBuilder.baseUrl(networkModificationServerBaseUri).build();
    }

    public List<ModificationInfos> getModifications(List<UUID> modificationsUuids) {
        if (CollectionUtils.isEmpty(modificationsUuids)) {
            return List.of();
        }

        String path = UriComponentsBuilder.fromPath(DELIMITER + NETWORK_MODIFICATION_SERVER_API_VERSION + DELIMITER +
                "network-composite-modifications" + DELIMITER + "network-modifications")
            .queryParam("uuids", modificationsUuids.toArray())
            .queryParam("onlyMetadata", "false")
            .buildAndExpand()
            .toUriString();

        return networkModificationServerRest.get()
            .uri(path)
            .retrieve()
            .body(new ParameterizedTypeReference<>() { });
    }
}
