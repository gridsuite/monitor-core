/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.external.client;

import com.powsybl.cases.datasource.CaseDataSourceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RestClientTest(CaseRestClient.class)
class CaseRestClientTest {

    @Autowired
    private CaseRestClient caseRestClient;

    @Test
    void getCaseDataSourceShouldReturnCaseDataSourceClient() {
        UUID caseUuid = UUID.randomUUID();

        CaseDataSourceClient result = caseRestClient.getCaseDataSource(caseUuid);

        assertThat(result).isNotNull().isInstanceOf(CaseDataSourceClient.class);
    }
}
