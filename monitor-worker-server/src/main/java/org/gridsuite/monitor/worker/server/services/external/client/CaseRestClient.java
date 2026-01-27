/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.external.client;

import com.powsybl.cases.datasource.CaseDataSourceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class CaseRestClient {

    private final RestTemplate caseServerRest;

    public CaseRestClient(@Value("${powsybl.services.case-server.base-uri:http://case-server/}") String caseServerBaseUri,
                          RestTemplateBuilder restTemplateBuilder) {
        this.caseServerRest = restTemplateBuilder.rootUri(caseServerBaseUri).build();
    }

    public CaseDataSourceClient getCaseDataSource(UUID caseUuid) {
        return new CaseDataSourceClient(caseServerRest, caseUuid);
    }
}
