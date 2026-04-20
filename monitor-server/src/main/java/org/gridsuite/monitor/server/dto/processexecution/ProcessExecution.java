/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.dto.processexecution;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Schema(description = "Process execution data")
@Builder
public record ProcessExecution(
    @NotNull
    UUID id,
    @NotNull
    String type,
    @NotNull
    UUID caseUuid,
    @NotNull
    UUID processConfigId,
    @NotNull
    ProcessStatus status,
    @NotNull
    String executionEnvName,
    Instant scheduledAt,
    Instant startedAt,
    Instant completedAt,
    @NotNull
    String userId
) { }
