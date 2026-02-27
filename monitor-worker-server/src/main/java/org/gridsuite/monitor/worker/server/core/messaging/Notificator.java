package org.gridsuite.monitor.worker.server.core.messaging;

import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStep;

import java.util.List;
import java.util.UUID;

/**
 * Used by the orchestrator to publish process execution updates (execution status and step statuses)
 * to the monitor server (via messaging).
 * <p>
 * Implementations are responsible for transporting these updates (e.g. Spring Cloud Stream),
 * including any required message headers/metadata for correlation (such as the {@code executionId})
 * and a discriminator for the update kind (execution vs single step vs batch step update via {@code MessageType}).
 * <p>
 * Expected usage pattern:
 * <ul>
 *   <li>Publish an initial batch of steps in {@code SCHEDULED} state</li>
 *   <li>Publish {@code RUNNING} execution status</li>
 *   <li>For each step, publish {@code RUNNING} then {@code COMPLETED}/{@code FAILED}/{@code SKIPPED}</li>
 *   <li>Publish final execution status ({@code COMPLETED} or {@code FAILED})</li>
 * </ul>
 */
public interface Notificator {

    /**
     * Publish an update about the overall process execution status.
     *
     * @param executionId the process execution identifier used to correlate all updates of the same run
     * @param update the execution status update payload (status, timestamps, environment, etc.)
     */
    void updateExecutionStatus(UUID executionId, ProcessExecutionStatusUpdate update);

    /**
     * Publish a batch update containing the current statuses of all steps for an execution.
     * <p>
     * This is typically sent at startup to initialize the monitor with all steps in {@code SCHEDULED}
     * (or to resynchronize the full step list).
     *
     * @param executionId the process execution identifier used to correlate all updates of the same run
     * @param steps the ordered list of step status snapshots to publish
     */
    void updateStepsStatuses(UUID executionId, List<ProcessExecutionStep> steps);

    /**
     * Publish an update for a single step status change (e.g. {@code RUNNING}, {@code COMPLETED},
     * {@code FAILED}, {@code SKIPPED}).
     *
     * @param executionId the process execution identifier used to correlate all updates of the same run
     * @param step the step status snapshot to publish
     */
    void updateStepStatus(UUID executionId, ProcessExecutionStep step);
}
