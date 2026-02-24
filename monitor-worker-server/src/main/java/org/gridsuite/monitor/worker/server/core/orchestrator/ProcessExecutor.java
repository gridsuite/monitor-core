package org.gridsuite.monitor.worker.server.core.orchestrator;

import org.gridsuite.monitor.commons.api.types.message.ProcessRunMessage;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;

/**
 * Root orchestrator interface responsible for executing a {@code Process} run request.
 * <p>
 * Typical responsibilities of an implementation include:
 * <ul>
 *   <li>Resolving the {@code Process} implementation from the {@code processType} contained in the run message</li>
 *   <li>Creating the {@code ProcessExecutionContext} from message data (execution id, case id, config, environment)</li>
 *   <li>Publishing initial step list/statuses (usually {@code SCHEDULED}) before running</li>
 *   <li>Publishing execution status transitions (e.g. {@code RUNNING} then {@code COMPLETED}/{@code FAILED})</li>
 *   <li>Orchestrating step execution via a {@link StepExecutor}</li>
 * </ul>
 * <p>
 * Error handling contract: if the execution fails (process not found, step throws, etc.), the implementation
 * should publish a {@code FAILED} execution status and rethrow the exception to the caller.
 */
public interface ProcessExecutor {

    /**
     * Execute a process run described by the given message.
     *
     * @param runMessage run request containing execution id, case uuid, process type and typed configuration payload
     * @param <T> concrete {@link ProcessConfig} type associated with the target {@code Process}
     * @throws IllegalArgumentException if no process is registered for {@code runMessage.processType()}
     * @throws RuntimeException if the process or one of its steps throws during execution (implementation should
     *                          update execution status accordingly before propagating)
     */
    <T extends ProcessConfig> void executeProcess(ProcessRunMessage<T> runMessage);
}
