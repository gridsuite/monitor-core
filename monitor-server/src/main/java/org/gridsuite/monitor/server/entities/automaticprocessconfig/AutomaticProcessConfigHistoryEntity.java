/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.entities.automaticprocessconfig;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.AbstractProcessConfigEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@Entity
@Table(name = "automatic_process_config_history")
@Getter
@Setter
@NoArgsConstructor
public class AutomaticProcessConfigHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_type", nullable = false, length = 32)
    private ProcessType processType;

    @ManyToOne
    @JoinColumn(
            name = "config_uuid",
            foreignKey = @ForeignKey(name = "automaticProcessConfigHistory_processConfig_fk")
    )
    private AbstractProcessConfigEntity processConfig;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;
}
