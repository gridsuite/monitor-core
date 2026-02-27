/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.entities.processexecution;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.monitor.commons.api.types.result.ResultType;
import org.gridsuite.monitor.commons.api.types.processexecution.StepStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "process_execution_step")
public class ProcessExecutionStepEntity {

    @Id
    private UUID id;

    @Column
    private String stepType;

    @Column
    private Integer stepOrder;

    @Column
    @Enumerated(EnumType.STRING)
    private StepStatus status;

    @Column
    private UUID resultId;

    @Column
    @Enumerated(EnumType.STRING)
    private ResultType resultType;

    @Column
    private UUID reportId;

    @Column
    private Instant startedAt;

    @Column
    private Instant completedAt;
}
