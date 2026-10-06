/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowProvider;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;

import java.util.Map;
import java.util.Objects;

/**
 * @author Antoine Bouhours {@literal <antoine.bouhours at rte-france.com>}
 */
public final class LoadFlowParametersService {

    private LoadFlowParametersService() {
    }

    public static LoadFlowParameters buildParameters(LoadFlowParametersInfos parametersInfos) {
        Objects.requireNonNull(parametersInfos);
        return buildParameters(parametersInfos, parametersInfos.getProvider());
    }

    public static LoadFlowParameters buildParameters(LoadFlowParametersInfos parametersInfos, String selectedProvider) {
        Objects.requireNonNull(parametersInfos);
        LoadFlowParameters parameters = parametersInfos.getCommonParameters() != null
                ? parametersInfos.getCommonParameters() : LoadFlowParameters.load();
        Map<String, Map<String, String>> perProvider = parametersInfos.getSpecificParametersPerProvider();
        if (perProvider == null || perProvider.isEmpty()) {
            return parameters;
        }
        String provider = selectedProvider != null ? selectedProvider : LoadFlow.find().getName();
        Map<String, String> specificParameters = perProvider.get(provider);
        if (specificParameters == null || specificParameters.isEmpty()) {
            return parameters;
        }

        LoadFlowProvider loadFlowProvider = LoadFlowProvider.findAll().stream()
                .filter(p -> p.getName().equals(provider))
                .findFirst().orElseThrow(() -> new PowsyblException("Loadflow provider not found " + provider));
        Extension<LoadFlowParameters> extension = loadFlowProvider.loadSpecificParameters(PlatformConfig.defaultConfig())
                .orElseThrow(() -> new PowsyblException("Cannot load specific loadflow parameters for provider " + provider));
        parameters.addExtension((Class) extension.getClass(), extension);
        loadFlowProvider.updateSpecificParameters(extension, specificParameters);
        return parameters;
    }
}
