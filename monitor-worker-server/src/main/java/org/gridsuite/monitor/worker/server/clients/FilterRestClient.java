/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import org.gridsuite.actions.FilterProvider;
import org.gridsuite.filter.Filter;
import org.gridsuite.filter.dto.AbstractFilter;
import org.gridsuite.modification.context.dto.FilterWithDistributionKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class FilterRestClient implements FilterProvider {
    private static final String FILTER_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";
    private static final String STANDALONE_FILTERS_URI = "/filters/standalone-filters";
    private static final String IDS_PARAM = "ids";
    private static final ParameterizedTypeReference<Map<UUID, Filter>> STANDALONE_FILTERS_BY_ID = new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<Map<UUID, FilterWithDistributionKeys>> STANDALONE_FILTERS_WITH_DISTRIBUTION_KEYS_BY_ID = new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public FilterRestClient(@Value("${gridsuite.services.filter-server.base-uri:http://filter-server/}") String filterServerBaseUri,
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

    public Map<UUID, Filter> getStandaloneFilters(List<UUID> filtersUuids) {
        return restClient.get()
            .uri(buildStandaloneFiltersUri(STANDALONE_FILTERS_URI, filtersUuids))
            .retrieve()
            .body(STANDALONE_FILTERS_BY_ID);
    }

    public Map<UUID, FilterWithDistributionKeys> getStandaloneFiltersWithDistributionKeys(List<UUID> filtersUuids) {
        return restClient.get()
            .uri(buildStandaloneFiltersUri(STANDALONE_FILTERS_URI + "/with-distribution-keys", filtersUuids))
            .retrieve()
            .body(STANDALONE_FILTERS_WITH_DISTRIBUTION_KEYS_BY_ID);
    }

    private String buildStandaloneFiltersUri(String path, List<UUID> filtersUuids) {
        return UriComponentsBuilder.fromPath(path)
            .queryParam(IDS_PARAM, filtersUuids)
            .buildAndExpand()
            .toUriString();
    }
}
