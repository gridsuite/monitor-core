/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.internal;

import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.commons.ResultType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ResultService {

    private final Map<ResultType, ResultProvider> providers;

    public ResultService(List<ResultProvider> resultProviders) {
        this.providers = resultProviders.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ResultProvider::getType,
                        Function.identity()
                ));
    }

    public ResultProvider getProvider(ResultType resultType) {
        ResultProvider provider = providers.get(resultType);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported result type: " + resultType);
        }
        return provider;
    }

    public String getResult(ResultInfos resultInfos) {
        return getProvider(resultInfos.resultType()).getResult(resultInfos.resultUUID());
    }

    public void deleteResult(ResultInfos resultInfos) {
        getProvider(resultInfos.resultType()).deleteResult(resultInfos.resultUUID());
    }
}
