/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import lombok.Setter;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class LoadFlowRestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowRestService.class);
    static final String LOADFLOW_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestTemplate restTemplate;

    @Setter
    private String loadFlowServerBaseUri;

    public LoadFlowRestService(
        RestTemplateBuilder restTemplateBuilder,
        @Value("${gridsuite.services.load-flow-server.base-uri:http://load-flow-server/}") String loadFlowServerBaseUri) {
        this.loadFlowServerBaseUri = loadFlowServerBaseUri;
        this.restTemplate = restTemplateBuilder.build();
    }

    public LoadFlowParametersInfos getParameters(UUID loadFlowParametersUuid) {
        LOGGER.info("Get loadflow parameters {}", loadFlowParametersUuid);

        var path = loadFlowServerBaseUri + UriComponentsBuilder.fromPath(DELIMITER + LOADFLOW_API_VERSION + DELIMITER + "parameters/{uuid}")
            .buildAndExpand(loadFlowParametersUuid)
            .toUriString();

        return restTemplate.getForObject(path, LoadFlowParametersInfos.class);
    }
}
