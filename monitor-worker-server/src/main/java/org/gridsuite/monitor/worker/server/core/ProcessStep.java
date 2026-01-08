/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core;

import org.gridsuite.monitor.commons.ProcessConfig;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public interface ProcessStep<C extends ProcessConfig> {

    ProcessStepType getType();

    void execute(ProcessStepExecutionContext<C> context);

}
