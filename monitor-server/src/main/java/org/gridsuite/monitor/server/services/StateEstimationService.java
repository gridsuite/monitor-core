/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * @author Achour BERRAHMA <achour.berrahma at rte-france.com>
 */
@Service
public class StateEstimationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StateEstimationService.class);
    static final String SE_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestTemplate restTemplate;

    @Setter
    private String stateEstimationServerBaseUri;

    private String getStateEstimationServerBaseUri() {
        return this.stateEstimationServerBaseUri + DELIMITER + SE_API_VERSION + DELIMITER;
    }

    public StateEstimationService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${gridsuite.services.state-estimation-server.base-uri:http://state-estimation-server/}") String stateEstimationServerBaseUri) {
        this.stateEstimationServerBaseUri = stateEstimationServerBaseUri;
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getResult(UUID resultUuid) {
        LOGGER.info("Fetching state estimation result {}", resultUuid);

        var path = UriComponentsBuilder.fromPath("/results/{resultUuid}")
                .buildAndExpand(resultUuid)
                .toUriString();

        return restTemplate.exchange(getStateEstimationServerBaseUri() + path, HttpMethod.GET, null, String.class).getBody();
    }

    public void deleteResult(UUID resultUuid) {
        LOGGER.info("Deleting state estimation result {}", resultUuid);

        var path = UriComponentsBuilder.fromPath("/results")
                .queryParam("resultsUuids", List.of(resultUuid))
                .build()
                .toUriString();

        restTemplate.delete(getStateEstimationServerBaseUri() + path);
    }
}
