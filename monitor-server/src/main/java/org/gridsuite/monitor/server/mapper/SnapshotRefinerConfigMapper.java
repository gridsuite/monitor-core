package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.PersistedProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SnapshotRefinerConfig;
import org.gridsuite.monitor.server.entities.SnapshotRefinerConfigEntity;

import java.util.Collections;
import java.util.Optional;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public final class SnapshotRefinerConfigMapper {

    private SnapshotRefinerConfigMapper() {
    }

    public static SnapshotRefinerConfigEntity toEntity(SnapshotRefinerConfig dto) {
        SnapshotRefinerConfigEntity entity = new SnapshotRefinerConfigEntity();
        entity.setProcessType(ProcessType.SNAPSHOT_REFINER);
        entity.setModificationUuids(Collections.emptyList());
        update(entity, dto);
        return entity;
    }

    public static PersistedProcessConfig toDto(SnapshotRefinerConfigEntity entity) {
        return new PersistedProcessConfig(entity.getId(), new SnapshotRefinerConfig(
                Optional.ofNullable(entity.getLoadFlowParametersUuid()),
                Optional.ofNullable(entity.getStateEstimationParametersUuid())
        ));
    }

    public static void update(SnapshotRefinerConfigEntity entity, SnapshotRefinerConfig dto) {
        entity.setLoadFlowParametersUuid(dto.loadFlowParametersUuid()
                .orElse(null));
        entity.setStateEstimationParametersUuid(dto.stateEstimationParametersUuid()
                .orElse(null));
    }
}
