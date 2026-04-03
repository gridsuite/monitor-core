/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStatusUpdate;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.server.services.processexecution.ProcessExecutionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Server-side {@link Notificator} implementation that writes status updates directly to the database
 * via {@link ProcessExecutionService}, instead of publishing them over RabbitMQ.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class ServerNotificator implements Notificator {

    private final ProcessExecutionService processExecutionService;

    @Override
    public void updateExecutionStatus(UUID executionId, ProcessExecutionStatusUpdate update) {
        processExecutionService.updateExecutionStatus(
            executionId,
            update.getStatus(),
            update.getExecutionEnvName(),
            update.getStartedAt(),
            update.getCompletedAt()
        );
    }

    @Override
    public void updateStepsStatuses(UUID executionId, List<ProcessExecutionStep> steps) {
        processExecutionService.updateStepsStatuses(executionId, steps);
    }

    @Override
    public void updateStepStatus(UUID executionId, ProcessExecutionStep step) {
        processExecutionService.updateStepStatus(executionId, step);
    }
}
