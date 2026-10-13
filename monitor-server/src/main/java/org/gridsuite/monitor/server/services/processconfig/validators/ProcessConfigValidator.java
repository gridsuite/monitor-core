package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

public interface ProcessConfigValidator<C extends ProcessConfig> {
    ProcessType getProcessType();

    void validate(C config);
}
