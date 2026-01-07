package org.gridsuite.process.worker.server.core;

import lombok.Getter;
import org.gridsuite.process.commons.ProcessConfig;

@Getter
public abstract class AbstractProcessStep<C extends ProcessConfig> implements ProcessStep<C> {

    private final ProcessStepType type;

    protected AbstractProcessStep(ProcessStepType type) {
        this.type = type;
    }
}
