/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.entities.automaticprocessconfig;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

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
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_type", nullable = false, length = 32)
    private ProcessType processType;

    @Column(name = "config_uuid", nullable = false)
    private UUID configUuid;

    @Column(name = "config_name")
    private String configName;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;
}
