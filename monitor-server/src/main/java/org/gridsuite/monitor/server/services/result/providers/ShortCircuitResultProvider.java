/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result.providers;

import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.clients.ShortCircuitRestClient;
import org.gridsuite.monitor.server.services.result.ResultProvider;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Service
public class ShortCircuitResultProvider implements ResultProvider {
    private final ShortCircuitRestClient shortCircuitRestClient;

    public ShortCircuitResultProvider(ShortCircuitRestClient shortCircuitRestClient) {
        this.shortCircuitRestClient = shortCircuitRestClient;
    }

    @Override
    public ResultType getType() {
        return ResultType.SHORT_CIRCUIT;
    }

    @Override
    public String getResult(UUID resultId) {
        return shortCircuitRestClient.getResult(resultId);
    }

    @Override
    public void deleteResult(UUID resultId) {
        shortCircuitRestClient.deleteResult(resultId);
    }
}
