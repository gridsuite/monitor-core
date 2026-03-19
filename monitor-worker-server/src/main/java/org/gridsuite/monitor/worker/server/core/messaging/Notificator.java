/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.messaging;

import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;

import java.util.List;
import java.util.UUID;

/**
 * Used by the orchestrator to publish process execution updates (execution status and step statuses)
 * to the monitor server (via messaging).
 *
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
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
