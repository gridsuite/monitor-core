/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.ForeignKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.monitor.commons.ProcessType;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.DiscriminatorType.STRING;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Entity
@Table(name = "process_config")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "process_type", discriminatorType = STRING)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public abstract class AbstractProcessConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "process_config_modifications",
                    joinColumns = @JoinColumn(name = "process_config_id"),
                    foreignKey = @ForeignKey(name = "AbstractProcessConfigEntity_modificationUuids_fk1"))
    @Column(name = "modification_uuid")
    @OrderColumn(name = "pos_modifications")
    private List<UUID> modificationUuids;

    public abstract ProcessType getType();
}




