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

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@Entity
@Table(name = "automatic_process_config")
@Getter
@Setter
@NoArgsConstructor
public class AutomaticProcessConfigEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "process_type", nullable = false, length = 32)
    private ProcessType processType;

    @OneToOne
    @JoinColumn(
            name = "config_uuid",
            foreignKey = @ForeignKey(name = "automaticProcessConfig_processConfig_fk")
    )
    private AbstractProcessConfigEntity processConfig;
}
