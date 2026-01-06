package org.gridsuite.process.worker.server.core;

import lombok.Getter;
import lombok.Setter;
import org.gridsuite.process.commons.ProcessConfig;

import java.util.UUID;

@Getter
public abstract class AbstractProcessStep<C extends ProcessConfig> implements ProcessStep<C> {

    private final ProcessStepType type;

    @Setter
    private UUID previousStepId;

    protected AbstractProcessStep(ProcessStepType type) {
        this.type = type;
    }
}
