/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.cases.datasource.CaseDataSourceClient;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class NetworkConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkConversionService.class);

    private final String caseServerBaseUri;

    public NetworkConversionService(@Value("${powsybl.services.case-server.base-uri:http://case-server/}") String caseServerBaseUri) {
        this.caseServerBaseUri = caseServerBaseUri;
    }

    public Network createNetwork(UUID caseUuid, ReportNode reporter) {
        LOGGER.info("Creating network");
        CaseDataSourceClient dataSource = new CaseDataSourceClient(caseServerBaseUri, caseUuid);
        return Network.read(dataSource, new Properties(), reporter);
    }
}
