/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.springframework.stereotype.Component;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Component
public class ProcessExecutionStepMapper {
    public ProcessExecutionStep toDto(ProcessExecutionStepEntity entity) {
        return new ProcessExecutionStep(
            entity.getId(),
            entity.getStepType(),
            entity.getStepOrder(),
            entity.getStatus(),
            entity.getResultId(),
            entity.getResultType(),
            entity.getReportId(),
            entity.getStartedAt(),
            entity.getCompletedAt());
    }
}
