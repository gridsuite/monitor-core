package org.gridsuite.monitor.worker.server.core.orchestrator;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;

/**
 * Child orchestrator interface responsible for executing a single {@link ProcessStep} within a process run.
 */
public interface StepExecutor {

    /**
     * Mark a step as skipped and publish the corresponding update.
     *
     * @param context step execution context
     * @param step step definition being skipped
     * @param <C> concrete {@link ProcessConfig} type associated with the parent process
     */
    <C extends ProcessConfig> void skipStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step);

    /**
     * Execute a step and publish step status updates around the execution.
     *
     * @param context step execution context
     * @param step step definition to execute
     * @param <C> concrete {@link ProcessConfig} type associated with the parent process
     * @throws RuntimeException any exception thrown by the step implementation is propagated after updating status
     */
    <C extends ProcessConfig> void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step);
}
