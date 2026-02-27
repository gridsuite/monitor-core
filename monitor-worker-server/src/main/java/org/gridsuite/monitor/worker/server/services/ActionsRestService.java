/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.worker.server.services;

import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.actions.dto.contingency.PersistentContingencyList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ActionsRestService {
    private static final String ACTIONS_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestTemplate actionsServerRest;
    private final String actionsServerBaseUri;

    public ActionsRestService(@Value("${gridsuite.services.actions-server.base-uri:http://actions-server/}") String actionsServerBaseUri,
                              RestTemplateBuilder restTemplateBuilder) {
        this.actionsServerRest = restTemplateBuilder.build();
        this.actionsServerBaseUri = actionsServerBaseUri;
    }

    public List<PersistentContingencyList> getPersistentContingencyLists(List<UUID> contingenciesUuids) {
        if (CollectionUtils.isEmpty(contingenciesUuids)) {
            return List.of();
        }

        String path = this.actionsServerBaseUri + UriComponentsBuilder
            .fromPath(DELIMITER + ACTIONS_SERVER_API_VERSION + DELIMITER + "contingency-lists")
            .buildAndExpand()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<UUID>> httpEntity = new HttpEntity<>(contingenciesUuids, headers);

        ResponseEntity<List<PersistentContingencyList>> response = actionsServerRest.exchange(path, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<>() { });
        return response.getBody() != null ? response.getBody() : List.of();
    }
}
