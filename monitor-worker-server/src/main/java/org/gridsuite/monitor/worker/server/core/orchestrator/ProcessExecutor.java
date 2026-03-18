package org.gridsuite.monitor.worker.server.core.orchestrator;

import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;

/**
 * Root orchestrator interface responsible for executing a {@code Process} run request.
 */
public interface ProcessExecutor {

    /**
     * Execute a process run described by the given message.
     *
     * @param runMessage run request
     * @param <T> concrete {@link ProcessConfig} type associated with the target {@code Process}
     * @throws IllegalArgumentException if no process is registered for {@code runMessage.processType()}
     * @throws RuntimeException if the process or one of its steps throws during execution (implementation should
     *                          update execution status accordingly before propagating)
     */
    <T extends ProcessConfig> void executeProcess(ProcessRunMessage<T> runMessage);
}
