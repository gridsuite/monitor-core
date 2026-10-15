package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.springframework.stereotype.Service;

@Service
public class ShortCircuitConfigValidator implements ProcessConfigValidator<ShortCircuitConfig> {
    @Override
    public ProcessType getProcessType() {
        return ProcessType.SHORT_CIRCUIT;
    }

    @Override
    public void validate(ShortCircuitConfig shortCircuitConfig) {
        // No specific validation required
    }
}
