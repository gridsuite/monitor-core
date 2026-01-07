package org.gridsuite.process.worker.server.core;

import org.gridsuite.process.commons.ProcessConfig;

import java.util.UUID;

public interface ProcessStep<C extends ProcessConfig> {

    ProcessStepType getType();

    void execute(ProcessStepExecutionContext<C> context);

}
