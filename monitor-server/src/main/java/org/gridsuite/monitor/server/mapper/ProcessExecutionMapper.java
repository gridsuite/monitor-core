/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.server.dto.ProcessExecution;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.springframework.stereotype.Component;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Component
public class ProcessExecutionMapper {
    public ProcessExecution toDto(ProcessExecutionEntity entity) {
        return new ProcessExecution(
            entity.getId(),
            entity.getType(),
            entity.getCaseUuid(),
            entity.getStatus(),
            entity.getExecutionEnvName(),
            entity.getScheduledAt(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getUserId());
    }
}
