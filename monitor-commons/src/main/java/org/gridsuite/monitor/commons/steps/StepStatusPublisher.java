/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.steps;

import org.gridsuite.monitor.commons.types.processexecution.ProcessExecutionStep;

import java.util.UUID;

/**
 * @author Mohamed Ben-rejeb <mohamed.ben-rejeb at rte-france.com>
 */
@FunctionalInterface
public interface StepStatusPublisher {

    /**
     * Publish an update for a single step status change (e.g. {@code RUNNING}, {@code COMPLETED},
     * {@code FAILED}, {@code SKIPPED}).
     *
     * @param executionId the process execution identifier used to correlate all updates of the same run
     * @param step the step status snapshot to publish
     */
    void updateStepStatus(UUID executionId, ProcessExecutionStep step);
}
