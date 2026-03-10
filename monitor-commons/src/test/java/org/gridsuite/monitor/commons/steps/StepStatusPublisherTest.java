/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.steps;

import org.gridsuite.monitor.commons.types.processexecution.ProcessExecutionStep;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Mohamed Ben-rejeb <mohamed.ben-rejeb at rte-france.com>
 */
class StepStatusPublisherTest {

    @Test
    void updateStepStatusShouldForwardExecutionIdAndStepToImplementation() {
        AtomicReference<UUID> publishedExecutionId = new AtomicReference<>();
        AtomicReference<ProcessExecutionStep> publishedStep = new AtomicReference<>();
        StepStatusPublisher stepStatusPublisher = (executionId, processExecutionStep) -> {
            publishedExecutionId.set(executionId);
            publishedStep.set(processExecutionStep);
        };

        UUID executionId = UUID.randomUUID();
        ProcessExecutionStep step = ProcessExecutionStep.builder().stepType("step").build();

        stepStatusPublisher.updateStepStatus(executionId, step);

        assertEquals(executionId, publishedExecutionId.get());
        assertSame(step, publishedStep.get());
    }
}
