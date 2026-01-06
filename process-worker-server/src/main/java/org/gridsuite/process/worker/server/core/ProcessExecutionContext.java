package org.gridsuite.process.worker.server.core;

import com.powsybl.iidm.network.Network;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.process.commons.ProcessConfig;

import java.util.UUID;

@Getter
public class ProcessExecutionContext<C extends ProcessConfig> {

    private final UUID executionId;
    private final C config;
    @Setter
    private Network network;
    private final String executionEnvName;
    @Setter
    private UUID lastExecutedStepId;

    public ProcessExecutionContext(C config, String executionEnvName) {
        this.config = config;
        this.executionId = config.executionId();
        this.executionEnvName = executionEnvName;
    }

    public ProcessStepExecutionContext<C> createStepContext(ProcessStep<? super C> step) {
        return new ProcessStepExecutionContext<>(this, config, step.getType());
    }
}
