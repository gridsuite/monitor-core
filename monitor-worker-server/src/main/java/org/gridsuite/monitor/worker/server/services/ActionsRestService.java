/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.worker.server.services;

import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.actions.dto.contingency.AbstractContingencyList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ActionsRestService {
    private static final String ACTIONS_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient actionsServerRest;

    public ActionsRestService(@Value("${gridsuite.services.actions-server.base-uri:http://actions-server/}") String actionsServerBaseUri,
                              RestClient.Builder restClientBuilder) {
        this.actionsServerRest = restClientBuilder
            .baseUrl(actionsServerBaseUri + DELIMITER + ACTIONS_SERVER_API_VERSION)
            .build();
    }

    public List<AbstractContingencyList> getPersistentContingencyLists(List<UUID> contingenciesUuids) {
        if (CollectionUtils.isEmpty(contingenciesUuids)) {
            return List.of();
        }

        return actionsServerRest.post()
            .uri("/contingency-lists")
            .contentType(MediaType.APPLICATION_JSON)
            .body(contingenciesUuids)
            .retrieve()
            .body(new ParameterizedTypeReference<>() { });
    }
}
