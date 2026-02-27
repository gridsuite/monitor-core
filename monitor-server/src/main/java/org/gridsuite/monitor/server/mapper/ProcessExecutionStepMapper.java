/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.api.types.processexecution.ProcessExecutionStep;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionStepEntity;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class ProcessExecutionStepMapper {
    public static ProcessExecutionStep toDto(ProcessExecutionStepEntity entity) {
        return ProcessExecutionStep.builder()
                .id(entity.getId())
                .stepType(entity.getStepType())
                .stepOrder(entity.getStepOrder())
                .status(entity.getStatus())
                .resultId(entity.getResultId())
                .resultType(entity.getResultType())
                .reportId(entity.getReportId())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}
