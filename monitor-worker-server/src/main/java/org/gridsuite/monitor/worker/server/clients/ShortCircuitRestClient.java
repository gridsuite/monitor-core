/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.powsybl.commons.PowsyblException;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit.ShortCircuitParametersInfos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Caroline Jeandat <caroline.jeandat at rte-france.com>
 */
@Service
public class ShortCircuitRestClient {
    static final String SHORT_CIRCUIT_API_VERSION = "v1";

    private final RestClient restClient;

    public ShortCircuitRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.shortcircuit-server.base-uri:http://shortcircuit-server/}") String shortCircuitServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(shortCircuitServerBaseUri + "/" + SHORT_CIRCUIT_API_VERSION)
            .build();
    }

    public ShortCircuitParametersInfos getParameters(UUID shortCircuitParametersUuid) {
        if (shortCircuitParametersUuid == null) {
            throw new PowsyblException("Short circuit parameters UUID is null !!");
        }

        return restClient.get()
            .uri("/parameters/{shortCircuitParametersUuid}", shortCircuitParametersUuid)
            .retrieve()
            .body(ShortCircuitParametersInfos.class);
    }

    public void saveResult(UUID resultUuid, ShortCircuitAnalysisResult result) {
        Objects.requireNonNull(result);
        restClient.post()
            .uri("/results/{resultUuid}", resultUuid)
            .contentType(MediaType.APPLICATION_JSON)
            .body(result)
            .retrieve()
            .body(UUID.class);
    }
}
