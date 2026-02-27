package org.gridsuite.monitor.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Entity
@Table(name = "snapshot_refiner_config")
@DiscriminatorValue("SNAPSHOT_REFINER")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SnapshotRefinerConfigEntity extends ProcessConfigEntity {
    @Column(name = "load_flow_parameters_uuid")
    private UUID loadFlowParametersUuid;

    @Column(name = "state_estimation_parameters_uuid")
    private UUID stateEstimationParametersUuid;
}
