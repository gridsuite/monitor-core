/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.entities.processconfig;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
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
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;

import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Entity
@Table(name = "security_analysis_config")
@DiscriminatorValue("SECURITY_ANALYSIS")
@PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "securityAnalysisConfig_id_fk_constraint"))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SecurityAnalysisConfigEntity extends AbstractProcessConfigEntity {
    @Column(name = "parameters_uuid")
    private UUID parametersUuid;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_analysis_contingencies",
                    joinColumns = @JoinColumn(name = "security_analysis_config_id"),
                    foreignKey = @ForeignKey(name = "SecurityAnalysisConfigEntity_contingencies_fk1"))
    @Column(name = "contingency")
    @OrderColumn(name = "pos_contingencies")
    private List<String> contingencies;

    @Override
    public ProcessType getType() {
        return ProcessType.SECURITY_ANALYSIS;
    }
}




