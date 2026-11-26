package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.springframework.stereotype.Service;

import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROVIDER;

@Service
public class ProcessConfigValidationService {
    private final LoadflowRestClient loadflowRestClient;
    private final SecurityAnalysisRestClient securityAnalysisRestClient;

    public ProcessConfigValidationService(LoadflowRestClient loadflowRestClient, SecurityAnalysisRestClient securityAnalysisRestClient) {
        this.loadflowRestClient = loadflowRestClient;
        this.securityAnalysisRestClient = securityAnalysisRestClient;
    }

    // Validation currently performs REST calls and must therefore be executed outside of transactions
    public void validate(ProcessConfig processConfig) {
        switch (processConfig) {
            case LoadFlowConfig config ->
                validateProvider(loadflowRestClient.getParameterProvider(config.loadflowParametersUuid()));
            case SecurityAnalysisConfig config -> {
                validateProvider(loadflowRestClient.getParameterProvider(config.loadflowParametersUuid()));
                validateProvider(securityAnalysisRestClient.getParameterProvider(config.securityAnalysisParametersUuid()));
            }
            case ShortCircuitConfig _ -> {
                // No specific validation required
            }
            default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
        }
    }

    private static void validateProvider(String provider) {
        if (!"OpenLoadFlow".equals(provider)) {
            throw new MonitorServerException(UNSUPPORTED_PROVIDER, "The provider must be Open Load Flow");
        }
    }
}
