/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.client;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.AbstractFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class FilterRestClient {
    private static final String FILTER_SERVER_API_VERSION = "v1";

    private static final String DELIMITER = "/";

    private final String filterServerBaseUri;

    private final RestTemplate restTemplate;

    public FilterRestClient(@Value("${gridsuite.services.filter-server.base-uri:http://filter-server/}") String filterServerBaseUri,
                            RestTemplateBuilder restTemplateBuilder) {
        this.filterServerBaseUri = filterServerBaseUri;
        this.restTemplate = restTemplateBuilder.build();
    }

    public List<AbstractFilter> getFilters(List<UUID> filtersUuids) {
        String path = UriComponentsBuilder.fromPath(DELIMITER + FILTER_SERVER_API_VERSION + "/filters/metadata")
            .queryParam("ids", filtersUuids)
            .buildAndExpand()
            .toUriString();
        try {
            return restTemplate.exchange(filterServerBaseUri + path, HttpMethod.GET, null, new ParameterizedTypeReference<List<AbstractFilter>>() { }).getBody();
        } catch (HttpStatusCodeException e) {
            throw new PowsyblException("Error retrieving filters", e);
        }
    }
}
