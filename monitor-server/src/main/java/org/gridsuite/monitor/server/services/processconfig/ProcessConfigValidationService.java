package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.services.processconfig.validators.ProcessConfigValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProcessConfigValidationService {
    private final Map<ProcessType, ProcessConfigValidator<?>> processConfigValidators;

    public ProcessConfigValidationService(List<ProcessConfigValidator<?>> listValidators) {
        this.processConfigValidators = listValidators.stream().collect(Collectors.toMap(ProcessConfigValidator::getProcessType, Function.identity(), (h1, h2) -> {
            throw new IllegalStateException(String.format("Duplicate process config validators for process type: %s", h1.getProcessType()));
        }));
    }

    // Validation currently performs REST calls and must therefore be executed outside of transactions
    public void validate(ProcessConfig processConfig) {
        getValidator(processConfig.processType()).validate(processConfig);
    }

    @SuppressWarnings("unchecked")
    private <C extends ProcessConfig> ProcessConfigValidator<C> getValidator(ProcessType processType) {
        ProcessConfigValidator<?> validator = processConfigValidators.get(processType);

        if (validator == null) {
            throw new IllegalArgumentException("Unsupported process config type: " + processType);
        }

        return (ProcessConfigValidator<C>) validator;
    }
}