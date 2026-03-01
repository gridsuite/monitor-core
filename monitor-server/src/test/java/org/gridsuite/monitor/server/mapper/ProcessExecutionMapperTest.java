/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.server.dto.ProcessExecution;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Radouane Khouadri <radouane.khouadri at rte-france.com>
 */
class ProcessExecutionMapperTest {

    private final ProcessExecutionMapper mapper = Mappers.getMapper(ProcessExecutionMapper.class);

    @Test
    void toDto() {

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
                Collections.emptyList()
        );

        ProcessExecution dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto)
                .usingRecursiveComparison()
                .ignoringFields("debugFileLocation")
                .isEqualTo(entity);
    }
}
