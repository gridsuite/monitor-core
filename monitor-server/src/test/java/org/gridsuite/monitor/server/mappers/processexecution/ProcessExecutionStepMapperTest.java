/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processexecution;

import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionStepEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Radouane Khouadri <radouane.khouadri at rte-france.com>
 */
class ProcessExecutionStepMapperTest {

    private final ProcessExecutionStepMapper mapper = Mappers.getMapper(ProcessExecutionStepMapper.class);

    @Test
    void updateEntityFromEntity() {
        ProcessExecutionStepEntity source = new ProcessExecutionStepEntity(
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

        UUID targetID = UUID.randomUUID();
        ProcessExecutionStepEntity target = new ProcessExecutionStepEntity(
                targetID,
                "Step Type 2",
                1,
                StepStatus.COMPLETED,
                UUID.randomUUID(),
                ResultType.SECURITY_ANALYSIS,
                UUID.randomUUID(),
                Instant.now().plusSeconds(20),
                Instant.now().plusSeconds(30)
        );

        mapper.updateEntityFromEntity(source, target);

        assertThat(target.getId()).isEqualTo(targetID);

        assertThat(target)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(source);
    }
}
