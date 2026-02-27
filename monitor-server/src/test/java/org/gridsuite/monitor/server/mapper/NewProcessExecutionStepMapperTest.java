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
import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.commons.StepStatus;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.UUID;

class NewProcessExecutionStepMapperTest {

    private final NewProcessExecutionStepMapper mapper = NewProcessExecutionStepMapper.INSTANCE;

    @Test
    void testEntityToDto() {
        // Arrange: Create a ProcessExecutionStepEntity
        ProcessExecutionStepEntity entity = new ProcessExecutionStepEntity(
                UUID.randomUUID(),
                "Step Type",
                1,
                StepStatus.RUNNING,
                UUID.randomUUID(),
                ResultType.SECURITY_ANALYSIS,
                UUID.randomUUID(),
                Instant.now(),
                Instant.now().plusSeconds(10)
        );

        // Act: Map the entity to DTO
        ProcessExecutionStep dto = mapper.entityToDto(entity);

        // Assert: Verify the mapping
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getStepType()).isEqualTo(entity.getStepType());
        assertThat(dto.getStepOrder()).isEqualTo(entity.getStepOrder());
        assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
        assertThat(dto.getResultId()).isEqualTo(entity.getResultId());
        assertThat(dto.getResultType()).isEqualTo(entity.getResultType());
        assertThat(dto.getReportId()).isEqualTo(entity.getReportId());
        assertThat(dto.getStartedAt()).isEqualTo(entity.getStartedAt());
        assertThat(dto.getCompletedAt()).isEqualTo(entity.getCompletedAt());
    }

    @Test
    void testDtoToEntity() {
        // Arrange: Create a ProcessExecutionStep DTO
        ProcessExecutionStep dto = ProcessExecutionStep.builder()
                .id(UUID.randomUUID())
                .stepType("Step Type")
                .stepOrder(1)
                .status(StepStatus.RUNNING)
                .resultId(UUID.randomUUID())
                .resultType(ResultType.SECURITY_ANALYSIS)
                .reportId(UUID.randomUUID())
                .startedAt(Instant.now())
                .completedAt(Instant.now().plusSeconds(10))
                .build();

        // Act: Map the DTO to Entity
        ProcessExecutionStepEntity entity = mapper.dtoToEntity(dto);

        // Assert: Verify the mapping
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getStepType()).isEqualTo(dto.getStepType());
        assertThat(entity.getStepOrder()).isEqualTo(dto.getStepOrder());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
        assertThat(entity.getResultId()).isEqualTo(dto.getResultId());
        assertThat(entity.getResultType()).isEqualTo(dto.getResultType());
        assertThat(entity.getReportId()).isEqualTo(dto.getReportId());
        assertThat(entity.getStartedAt()).isEqualTo(dto.getStartedAt());
        assertThat(entity.getCompletedAt()).isEqualTo(dto.getCompletedAt());
    }
}
