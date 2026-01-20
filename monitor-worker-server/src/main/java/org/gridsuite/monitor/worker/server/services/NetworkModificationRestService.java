/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.modification.dto.ModificationInfos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
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

    public List<ModificationInfos> getModifications(List<UUID> modificationsUuids) {
        List<ModificationInfos> modifications = new ArrayList<>();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromUriString(DELIMITER + NETWORK_MODIFICATION_SERVER_API_VERSION + DELIMITER +
                "network-composite-modification" + DELIMITER + "{uuid}" + DELIMITER + "network-modifications?onlyMetadata=false");

        modificationsUuids.forEach(uuid -> {
            String path = this.networkModificationServerBaseUri + uriBuilder.buildAndExpand(uuid).toUriString();
            ModificationInfos[] modificationInfos = networkModificationServerRest.getForObject(path, ModificationInfos[].class);
            if (modificationInfos != null) {
                modifications.addAll(Arrays.asList(modificationInfos));
            }
        });

        return modifications;
    }
}
