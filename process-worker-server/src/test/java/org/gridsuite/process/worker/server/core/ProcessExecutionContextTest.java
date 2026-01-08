package org.gridsuite.process.worker.server.core;

import com.powsybl.iidm.network.Network;
import org.gridsuite.process.commons.ProcessConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessExecutionContextTest {

    @Mock
    private ProcessConfig config;

    @Mock
    private Network network;

    @Test
    void shouldInitializeCorrectly() {
        UUID executionId = UUID.randomUUID();
        String envName = "test-env";
        when(config.executionId()).thenReturn(executionId);

        ProcessExecutionContext<ProcessConfig> processContext = new ProcessExecutionContext<>(config, envName);

        assertThat(processContext.getConfig()).isEqualTo(config);
        assertThat(processContext.getExecutionId()).isEqualTo(executionId);
        assertThat(processContext.getExecutionEnvName()).isEqualTo(envName);
        assertThat(processContext.getNetwork()).isNull();
    }

    @Test
    void shouldSetAndGetNetwork() {
        when(config.executionId()).thenReturn(UUID.randomUUID());
        ProcessExecutionContext<ProcessConfig> processContext = new ProcessExecutionContext<>(config, "test-env");

        processContext.setNetwork(network);

        assertThat(processContext.getNetwork()).isEqualTo(network);
    }

    @Test
    void shouldCreateStepContext() {
        when(config.executionId()).thenReturn(UUID.randomUUID());
        ProcessExecutionContext<ProcessConfig> processContext = new ProcessExecutionContext<>(config, "test-env");
        ProcessStep<ProcessConfig> step = mock(ProcessStep.class);
        ProcessStepType stepType = mock(ProcessStepType.class);
        when(stepType.getName()).thenReturn("test-step");
        when(step.getType()).thenReturn(stepType);
        UUID previousStepId = UUID.randomUUID();

        ProcessStepExecutionContext<ProcessConfig> stepContext = processContext.createStepContext(step, previousStepId);

        assertThat(stepContext).isNotNull();
        assertThat(stepContext.getProcessContext()).isEqualTo(processContext);
        assertThat(stepContext.getConfig()).isEqualTo(config);
        assertThat(stepContext.getProcessStepType()).isEqualTo(stepType);
        assertThat(stepContext.getPreviousStepExecutionId()).isEqualTo(previousStepId);
    }
}
