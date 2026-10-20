/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.clients;

import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.worker.server.dto.processexecution.ExecutionStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * @author Radouane KHOUADRI <redouane.khouadri_externe at rte-france.com>
 */
@Service
public class MonitorRestClient {
    static final String MONITOR_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public MonitorRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.monitor-server.base-uri:http://monitor-server/}") String monitorServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(monitorServerBaseUri + DELIMITER + MONITOR_API_VERSION)
            .build();
    }

    public boolean isExecutionCanceled(UUID executionId) {
        return getExecutionStatus(executionId) == ProcessStatus.CANCELLED;
    }

    public boolean isExecutionCanceling(UUID executionId) {
        return getExecutionStatus(executionId) == ProcessStatus.CANCELLING;
    }

    private ProcessStatus getExecutionStatus(UUID executionId) {
        ExecutionStatusResponse response = restClient.get()
                .uri("/executions/{executionId}", executionId)
                .retrieve()
                .body(ExecutionStatusResponse.class);

        return response == null ? null : response.status();
    }
}
