/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.entities.processconfig;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;

import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Entity
@Table(name = "pcc_min_config")
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "pccMinConfig_id_fk_constraint"))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PccMinConfigEntity extends AbstractProcessConfigEntity {
    @Column(name = "short_circuit_parameters_uuid")
    private UUID shortCircuitParametersUuid;

    @Column(name = "pcc_min_parameters_uuid")
    private UUID pccMinParametersUuid;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "watched_generators",
                    joinColumns = @JoinColumn(name = "process_config_id"),
                    foreignKey = @ForeignKey(name = "PccMinConfigEntity_watched_generators_fk1"))
    @OrderColumn(name = "pos_watched_generators")
    private List<UUID> watchedGenerators;

    @Override
    public ProcessType getProcessType() {
        return ProcessType.PCC_MIN;
    }
}
