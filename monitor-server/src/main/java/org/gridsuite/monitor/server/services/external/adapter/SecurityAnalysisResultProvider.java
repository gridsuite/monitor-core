/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.external.adapter;

import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.server.services.internal.ResultProvider;
import org.gridsuite.monitor.server.services.external.client.SecurityAnalysisRestClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class SecurityAnalysisResultProvider implements ResultProvider {
    private final SecurityAnalysisRestClient securityAnalysisRestClient;

    public SecurityAnalysisResultProvider(SecurityAnalysisRestClient securityAnalysisRestClient) {
        this.securityAnalysisRestClient = securityAnalysisRestClient;
    }

    @Override
    public ResultType getType() {
        return ResultType.SECURITY_ANALYSIS;
    }

    @Override
    public String getResult(UUID resultId) {
        return securityAnalysisRestClient.getResult(resultId);
    }

    @Override
    public void deleteResult(UUID resultId) {
        securityAnalysisRestClient.deleteResult(resultId);
    }
}
