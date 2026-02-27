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

import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.server.dto.ProcessExecution;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NewProcessExecutionMapperTest {

    private final NewProcessExecutionMapper mapper = NewProcessExecutionMapper.INSTANCE;

    @Test
    void testEntityToDto() {
        // Arrange: Create a ProcessExecutionStepEntity
        ProcessExecutionEntity entity = new ProcessExecutionEntity(
                UUID.randomUUID(),
                "Process Type",
                UUID.randomUUID(),
                ProcessStatus.RUNNING,
                "TestEnv",
                Instant.now(),
                Instant.now(),
                Instant.now().plusSeconds(10),
                "user123",
                "location",
                Collections.EMPTY_LIST
        );

        // Act: Map the entity to DTO
        ProcessExecution dto = mapper.entityToDto(entity);

        // Assert: Verify the mapping
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.type()).isEqualTo(entity.getType());
        assertThat(dto.caseUuid()).isEqualTo(entity.getCaseUuid());
        assertThat(dto.status()).isEqualTo(entity.getStatus());
        assertThat(dto.executionEnvName()).isEqualTo(entity.getExecutionEnvName());
        assertThat(dto.scheduledAt()).isEqualTo(entity.getScheduledAt());
        assertThat(dto.startedAt()).isEqualTo(entity.getStartedAt());
        assertThat(dto.completedAt()).isEqualTo(entity.getCompletedAt());
        assertThat(dto.userId()).isEqualTo(entity.getUserId());
    }

    @Test
    void testDtoToEntity() {
        // Arrange: Create a ProcessExecutionStep DTO
        ProcessExecution dto = new ProcessExecution(
                UUID.randomUUID(),
                "TestType",
                UUID.randomUUID(),
                ProcessStatus.RUNNING,  // Example ProcessStatus enum value
                "TestEnv",
                Instant.now(),
                Instant.now(),
                Instant.now().plusSeconds(10),
                "user123"
        );

        // Act: Map the DTO to Entity
        ProcessExecutionEntity entity = mapper.dtoToEntity(dto);

        // Assert: Verify the mapping
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.id());
        assertThat(entity.getType()).isEqualTo(dto.type());
        assertThat(entity.getCaseUuid()).isEqualTo(dto.caseUuid());
        assertThat(entity.getStatus()).isEqualTo(dto.status());
        assertThat(entity.getExecutionEnvName()).isEqualTo(dto.executionEnvName());
        assertThat(entity.getScheduledAt()).isEqualTo(dto.scheduledAt());
        assertThat(entity.getStartedAt()).isEqualTo(dto.startedAt());
        assertThat(entity.getCompletedAt()).isEqualTo(dto.completedAt());
        assertThat(entity.getUserId()).isEqualTo(dto.userId());
    }
}
