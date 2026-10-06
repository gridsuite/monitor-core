/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Service
public class ProcessConfigValidator {
    private final Set<String> ALLOWED_PROVIDER = Set.of("OpenLoadFlow");

    private final LoadflowRestClient loadflowRestClient;
    private final SecurityAnalysisRestClient securityAnalysisRestClient;

    public ProcessConfigValidator(LoadflowRestClient loadflowRestClient, SecurityAnalysisRestClient securityAnalysisRestClient) {
        this.loadflowRestClient = loadflowRestClient;
        this.securityAnalysisRestClient = securityAnalysisRestClient;
    }

    public void validate(ProcessConfig processConfig) {
        switch (processConfig) {
            case SecurityAnalysisConfig saConfig:
                validate(saConfig);
                break;
            case LoadFlowConfig lfConfig:
                validate(lfConfig);
                break;
            case ShortCircuitConfig scConfig:
                // no check yet
                break;
            default:
                throw new IllegalStateException("Unsupported process config type: " + processConfig.processType());
        }
    }

    private void validate(SecurityAnalysisConfig securityAnalysisConfig) {
        String loadflowParametersProvider = loadflowRestClient.getParameterProvider(securityAnalysisConfig.loadflowParametersUuid());
        String saParametersProvider = securityAnalysisRestClient.getParameterProvider(securityAnalysisConfig.securityAnalysisParametersUuid());
        if(!ALLOWED_PROVIDER.contains(loadflowParametersProvider) ||
            !ALLOWED_PROVIDER.contains(saParametersProvider)) {
            throw new UnsupportedOperationException("The provider must be OLF");
        }
    }

    private void validate(LoadFlowConfig loadFlowConfig) {
        String parametersProvider = loadflowRestClient.getParameterProvider(loadFlowConfig.loadflowParametersUuid());
        if(!ALLOWED_PROVIDER.contains(parametersProvider)) {
            throw new UnsupportedOperationException("The provider must be OLF");
        }
    }
}
