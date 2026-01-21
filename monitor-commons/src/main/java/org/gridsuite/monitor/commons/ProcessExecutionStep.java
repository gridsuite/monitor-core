/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessExecutionStep {
    private UUID id;
    private String stepType;
    private Integer stepOrder;
    private StepStatus status;
    private UUID resultId;
    private ResultType resultType;
    private UUID reportId;
    private Instant startedAt;
    private Instant completedAt;
}
