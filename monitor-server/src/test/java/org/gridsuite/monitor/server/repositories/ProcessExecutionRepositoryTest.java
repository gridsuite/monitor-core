/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.repositories;

import org.gridsuite.monitor.commons.ProcessStatus;
import org.gridsuite.monitor.commons.StepStatus;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.gridsuite.monitor.server.entities.ProcessExecutionStepEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@DataJpaTest
class ProcessExecutionRepositoryTest {

    @Autowired
    private ProcessExecutionRepository executionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void stepsShouldBeOrderedByStepOrder() {
        ProcessExecutionEntity execution = ProcessExecutionEntity.builder()
                .type("SECURITY_ANALYSIS")
                .caseUuid(UUID.randomUUID())
                .status(ProcessStatus.RUNNING)
                .scheduledAt(Instant.now())
                .steps(new ArrayList<>())
                .build();

        // Add steps in non-sequential order (1 before 0)
        ProcessExecutionStepEntity step1 = ProcessExecutionStepEntity.builder()
                .id(UUID.randomUUID())
                .stepType("SECOND_STEP")
                .stepOrder(1)
                .status(StepStatus.COMPLETED)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .build();
        execution.getSteps().add(step1);
        ProcessExecutionStepEntity step0 = ProcessExecutionStepEntity.builder()
                .id(UUID.randomUUID())
                .stepType("FIRST_STEP")
                .stepOrder(0)
                .status(StepStatus.COMPLETED)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .build();
        execution.getSteps().add(step0);

        executionRepository.save(execution);
        // Force DB write and reload
        entityManager.flush();
        entityManager.clear();

        ProcessExecutionEntity retrieved = executionRepository.findById(execution.getId()).orElseThrow();
        assertThat(retrieved.getSteps()).hasSize(2);
        assertThat(retrieved.getSteps().get(0).getStepType()).isEqualTo("FIRST_STEP");
        assertThat(retrieved.getSteps().get(0).getStepOrder()).isZero();
        assertThat(retrieved.getSteps().get(1).getStepType()).isEqualTo("SECOND_STEP");
        assertThat(retrieved.getSteps().get(1).getStepOrder()).isEqualTo(1);
    }
}
