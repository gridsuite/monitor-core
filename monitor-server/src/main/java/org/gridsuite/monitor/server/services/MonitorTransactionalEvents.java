/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ResultInfos;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public final class MonitorTransactionalEvents {

    private MonitorTransactionalEvents() {
    }

    public record ProcessRunRequested(UUID caseUuid, ProcessConfig processConfig, UUID executionId, String debugFileLocation) {
    }

    public record ExecutionDeleted(List<ResultInfos> resultInfos, List<UUID> reportIds) {
    }
}
