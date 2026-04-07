/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class LoadFlowConfigMapperTest {

    private final LoadFlowConfigMapper mapper = Mappers.getMapper(LoadFlowConfigMapper.class);

    @Test
    void toEntity() {
        LoadFlowConfig dto = new LoadFlowConfig(
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );

        LoadFlowConfigEntity entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getLoadflowParametersUuid()).isEqualTo(dto.loadflowParametersUuid());
        assertThat(entity.getModificationUuids()).isEqualTo(dto.modificationUuids());
        assertThat(entity.getProcessType()).isEqualTo(dto.processType());
    }

    @Test
    void toDto() {
        LoadFlowConfigEntity entity = new LoadFlowConfigEntity();
        entity.setLoadflowParametersUuid(UUID.randomUUID());
        entity.setModificationUuids(List.of(UUID.randomUUID(), UUID.randomUUID()));
        entity.setProcessType(ProcessType.LOADFLOW);

        LoadFlowConfig dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.loadflowParametersUuid()).isEqualTo(entity.getLoadflowParametersUuid());
        assertThat(dto.modificationUuids()).isEqualTo(entity.getModificationUuids());
        assertThat(dto.processType()).isEqualTo(ProcessType.LOADFLOW);
    }
}
