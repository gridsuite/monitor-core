package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROVIDER;

@Service
public class SecurityAnalysisConfigValidator implements ProcessConfigValidator<SecurityAnalysisConfig> {
    private static final Set<String> ALLOWED_PROVIDER = Set.of("OpenLoadFlow");

    private final LoadflowRestClient loadflowRestClient;
    private final SecurityAnalysisRestClient securityAnalysisRestClient;

    public SecurityAnalysisConfigValidator(LoadflowRestClient loadflowRestClient, SecurityAnalysisRestClient securityAnalysisRestClient) {
        this.loadflowRestClient = loadflowRestClient;
        this.securityAnalysisRestClient = securityAnalysisRestClient;
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.SECURITY_ANALYSIS;
    }

    @Override
    public void validate(SecurityAnalysisConfig securityAnalysisConfig) {
        String loadflowParametersProvider = loadflowRestClient.getParameterProvider(securityAnalysisConfig.loadflowParametersUuid());
        String saParametersProvider = securityAnalysisRestClient.getParameterProvider(securityAnalysisConfig.securityAnalysisParametersUuid());
        if (!ALLOWED_PROVIDER.contains(loadflowParametersProvider) ||
            !ALLOWED_PROVIDER.contains(saParametersProvider)) {
            throw new MonitorServerException(UNSUPPORTED_PROVIDER, "The provider must be Open Load Flow");
        }
    }
}
