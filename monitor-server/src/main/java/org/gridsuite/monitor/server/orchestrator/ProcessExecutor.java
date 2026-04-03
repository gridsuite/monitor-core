/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator;

import org.gridsuite.monitor.commons.types.messaging.ProcessRunMessage;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;

/**
 * Root orchestrator interface responsible for executing a {@code Process} run request.
 *
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
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
