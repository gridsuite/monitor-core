package org.gridsuite.process.worker.server.core;

import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;

public interface Process<C extends ProcessConfig> {

    ProcessType getProcessType();

    void execute(ProcessExecutionContext<C> context);
}
