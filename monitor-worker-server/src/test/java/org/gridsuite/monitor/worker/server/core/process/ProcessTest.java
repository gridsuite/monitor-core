/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core.process;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessTest {

    @Test
    void getStepsShouldReturnUnmodifiableListOfDefinedSteps() {
        ProcessStep<ProcessConfig> step1 = mock(ProcessStep.class);
        ProcessStep<ProcessConfig> step2 = mock(ProcessStep.class);
        TestProcess process = new TestProcess(List.of(step1, step2));

        List<ProcessStep<ProcessConfig>> steps = process.getSteps();

        assertThat(steps).containsExactly(step1, step2);
        assertThat(steps).isUnmodifiable();
    }

    private static class TestProcess extends AbstractProcess<ProcessConfig> {
        private final List<ProcessStep<ProcessConfig>> steps;

        TestProcess(List<ProcessStep<ProcessConfig>> steps) {
            super(ProcessType.SECURITY_ANALYSIS);
            this.steps = steps;
        }

        @Override
        protected List<ProcessStep<ProcessConfig>> defineSteps() {
            return steps;
        }
    }
}
