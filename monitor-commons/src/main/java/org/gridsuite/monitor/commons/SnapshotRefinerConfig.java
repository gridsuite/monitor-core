package org.gridsuite.monitor.commons;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public record SnapshotRefinerConfig(
        Optional<UUID> loadFlowParametersUuid,
        Optional<UUID> stateEstimationParametersUuid
) implements ProcessConfig {

    @Override
    public ProcessType processType() {
        return ProcessType.SNAPSHOT_REFINER;
    }
}
