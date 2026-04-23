/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
public record ProcessDeletionInfos (UUID reportId, List<ResultInfos> resultInfos) {
    public static ProcessDeletionInfos fromProcessExecutionEntity(ProcessExecutionEntity entity) {
        return new ProcessDeletionInfos(
            entity.getReportId(),
            Optional.ofNullable(entity.getSteps()).orElse(List.of()).stream()
                .filter(step -> step.getResultId() != null && step.getResultType() != null)
                .map(step -> new ResultInfos(step.getResultId(), step.getResultType()))
                .toList()
        );
    }
}
