/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.context;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;

/**
 * A step definition paired with its execution context for a single process run.
 *
 * @param step step definition to execute
 * @param stepExecutionContext per-run step execution context
 * @param <C> concrete process configuration type shared by the step and its context
 */
public record StepWithContext<C extends ProcessConfig>(
        ProcessStep<C> step,
        ProcessStepExecutionContext<C> stepExecutionContext) {
}
