/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core;

import com.powsybl.iidm.network.Network;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.context.ProcessExecutionContext;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.ProcessStep;
import org.gridsuite.monitor.worker.server.core.process.ProcessStepType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessExecutionContextTest {

    @Mock
    private ProcessConfig config;

    @Mock
    private Network network;

    @Test
    void shouldInitializeCorrectly() {
        UUID executionId = UUID.randomUUID();
        UUID caseUuid = UUID.randomUUID();
        String envName = "test-env";

        ProcessExecutionContext<ProcessConfig> processContext = new ProcessExecutionContext<>(executionId, caseUuid, config, envName);

        assertThat(processContext.getConfig()).isEqualTo(config);
        assertThat(processContext.getExecutionId()).isEqualTo(executionId);
        assertThat(processContext.getCaseUuid()).isEqualTo(caseUuid);
        assertThat(processContext.getExecutionEnvName()).isEqualTo(envName);
        assertThat(processContext.getNetwork()).isNull();
    }

    @Test
    void shouldSetAndGetNetwork() {
        ProcessExecutionContext<ProcessConfig> processContext = new ProcessExecutionContext<>(UUID.randomUUID(), UUID.randomUUID(), config, "test-env");

        processContext.setNetwork(network);

        assertThat(processContext.getNetwork()).isEqualTo(network);
    }

    @Test
    void shouldCreateStepContext() {
        ProcessExecutionContext<ProcessConfig> processContext = new ProcessExecutionContext<>(UUID.randomUUID(), UUID.randomUUID(), config, "test-env");
        ProcessStep<ProcessConfig> step = mock(ProcessStep.class);
        ProcessStepType stepType = mock(ProcessStepType.class);
        when(stepType.getName()).thenReturn("test-step");
        when(step.getType()).thenReturn(stepType);
        int stepOrder = 3;

        ProcessStepExecutionContext<ProcessConfig> stepContext = processContext.createStepContext(step, stepOrder);

        assertThat(stepContext).isNotNull();
        assertThat(stepContext.getNetwork()).isEqualTo(processContext.getNetwork());
        assertThat(stepContext.getProcessExecutionId()).isEqualTo(processContext.getExecutionId());
        assertThat(stepContext.getConfig()).isEqualTo(config);
        assertThat(stepContext.getProcessStepType()).isEqualTo(stepType);
        assertThat(stepContext.getStepOrder()).isEqualTo(stepOrder);
    }
}
