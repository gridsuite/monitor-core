/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadflowParametersInfos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class LoadflowRestClient {
    static final String LOADFLOW_API_VERSION = "v1";

    private final RestClient restClient;

    public LoadflowRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.loadflow-server.base-uri:http://loadflow-server/}") String loadflowServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(loadflowServerBaseUri + "/" + LOADFLOW_API_VERSION)
            .build();
    }

    public LoadflowParametersInfos getParameters(UUID loadflowParametersUuid) {
        if (loadflowParametersUuid == null) {
            throw new PowsyblException("Loadflow parameters UUID is null !!");
        }

        return restClient.get()
            .uri("/parameters/{loadflowParametersUuid}", loadflowParametersUuid)
            .retrieve()
            .body(LoadflowParametersInfos.class);
    }
}
