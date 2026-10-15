package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROVIDER;

@Service
public class LoadFlowConfigValidator implements ProcessConfigValidator<LoadFlowConfig> {
    private static final Set<String> ALLOWED_PROVIDER = Set.of("OpenLoadFlow");
    private final LoadflowRestClient loadflowRestClient;

    public LoadFlowConfigValidator(LoadflowRestClient loadflowRestClient) {
        this.loadflowRestClient = loadflowRestClient;
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.LOADFLOW;
    }

    @Override
    public void validate(LoadFlowConfig loadFlowConfig) {
        String parametersProvider = loadflowRestClient.getParameterProvider(loadFlowConfig.loadflowParametersUuid());
        if (!ALLOWED_PROVIDER.contains(parametersProvider)) {
            throw new MonitorServerException(UNSUPPORTED_PROVIDER, "The provider must be Open Load Flow");
        }
    }
}
