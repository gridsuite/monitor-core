/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.actions.FilterProvider;
import org.gridsuite.filter.AbstractFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class FilterRestService implements FilterProvider {
    private static final String FILTER_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public FilterRestService(@Value("${gridsuite.services.filter-server.base-uri:http://filter-server/}") String filterServerBaseUri,
                             RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl(filterServerBaseUri + DELIMITER + FILTER_SERVER_API_VERSION)
            .build();
    }

    public List<AbstractFilter> getFilters(List<UUID> filtersUuids) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/filters/metadata")
                .queryParam("ids", filtersUuids.toArray())
                .build())
            .retrieve()
            .body(new ParameterizedTypeReference<>() { });
    }
}
