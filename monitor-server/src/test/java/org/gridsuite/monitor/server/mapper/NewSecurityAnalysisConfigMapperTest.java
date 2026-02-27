/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */

import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NewSecurityAnalysisConfigMapperTest {

    private final NewSecurityAnalysisConfigMapper mapper = NewSecurityAnalysisConfigMapper.INSTANCE;

    @Test
    void testEntityToDto() {
        // Arrange: Create a SecurityAnalysisConfigStepEntity
        SecurityAnalysisConfigEntity entity = new SecurityAnalysisConfigEntity(
                UUID.randomUUID(),
                List.of("C1", "C2", "C3")
        );
        entity.setModificationUuids(
                List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );

        // Act: Map the entity to DTO
        SecurityAnalysisConfig dto = mapper.entityToDto(entity);

        // Assert: Verify the mapping
        assertThat(dto).isNotNull();
        assertThat(dto.parametersUuid()).isEqualTo(entity.getParametersUuid());
        assertThat(dto.modificationUuids()).isEqualTo(entity.getModificationUuids());

    }

    @Test
    void testDtoToEntity() {
        // Arrange: Create a SecurityAnalysisConfigStep DTO
        SecurityAnalysisConfig dto = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of("C1", "C2", "C3"),
                List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );

        // Act: Map the DTO to Entity
        SecurityAnalysisConfigEntity entity = mapper.dtoToEntity(dto);

        // Assert: Verify the mapping
        assertThat(entity).isNotNull();
        assertThat(entity.getParametersUuid()).isEqualTo(dto.parametersUuid());
        assertThat(entity.getContingencies()).isEqualTo(dto.contingencies());
        assertThat(entity.getModificationUuids()).isEqualTo(dto.modificationUuids());
        assertThat(entity.getProcessType()).isEqualTo(dto.processType());
    }
}
