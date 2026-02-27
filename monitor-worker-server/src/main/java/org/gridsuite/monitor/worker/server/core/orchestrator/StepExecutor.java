package org.gridsuite.monitor.worker.server.core.orchestrator;

import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;

/**
 * Child orchestrator interface responsible for executing (or skipping) a single {@link ProcessStep} within a process run.
 * <p>
 * Implementations typically integrate cross-cutting concerns around step execution, such as:
 * <ul>
 *   <li>Publishing step status transitions (e.g. {@code RUNNING} then {@code COMPLETED}/{@code FAILED}/{@code SKIPPED})</li>
 *   <li>Triggering report publication after a step completes</li>
 *   <li>Enriching updates with report/result identifiers from the execution context</li>
 * </ul>
 * <p>
 * Error handling contract: {@link #executeStep(ProcessStepExecutionContext, ProcessStep)} should update the step
 * status to {@code FAILED} before rethrowing the underlying exception so that the orchestrator can decide whether
 * to stop, skip remaining steps, or mark the overall execution as failed.
 */
public interface StepExecutor {

    /**
     * Mark a step as skipped and publish the corresponding update.
     * <p>
     * This is generally used by the orchestrator after a previous step failure when it decides to skip remaining steps.
     *
     * @param context step execution context (execution id, step id, step order, timestamps, report/result info)
     * @param step step definition being skipped
     * @param <C> concrete {@link ProcessConfig} type associated with the parent process
     */
    <C extends ProcessConfig> void skipStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step);

    /**
     * Execute a step and publish step status updates around the execution.
     * <p>
     * Typical sequence:
     * <ol>
     *   <li>Publish {@code RUNNING}</li>
     *   <li>Invoke {@code step.execute(context)}</li>
     *   <li>On success: publish {@code COMPLETED} (and optionally publish/report side-effects)</li>
     *   <li>On error: publish {@code FAILED} then rethrow</li>
     * </ol>
     *
     * @param context step execution context (execution id, step id, step order, timestamps, report/result info)
     * @param step step definition to execute
     * @param <C> concrete {@link ProcessConfig} type associated with the parent process
     * @throws RuntimeException any exception thrown by the step implementation is propagated after updating status
     */
    <C extends ProcessConfig> void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step);
}
