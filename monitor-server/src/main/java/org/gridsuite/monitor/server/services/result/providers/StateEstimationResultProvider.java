/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result.providers;

import org.gridsuite.monitor.commons.api.types.result.ResultType;
import org.gridsuite.monitor.server.clients.StateEstimationRestClient;
import org.gridsuite.monitor.server.services.result.ResultProvider;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Achour BERRAHMA <achour.berrahma at rte-france.com>
 */
@Service
public class StateEstimationResultProvider implements ResultProvider {
    private final StateEstimationRestClient stateEstimationRestClient;

    public StateEstimationResultProvider(StateEstimationRestClient stateEstimationRestClient) {
        this.stateEstimationRestClient = stateEstimationRestClient;
    }

    @Override
    public ResultType getType() {
        return ResultType.STATE_ESTIMATION;
    }

    @Override
    public String getResult(UUID resultId) {
        return stateEstimationRestClient.getResult(resultId);
    }

    @Override
    public void deleteResult(UUID resultId) {
        stateEstimationRestClient.deleteResult(resultId);
    }
}
