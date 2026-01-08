package org.gridsuite.process.worker.server.core;

import com.powsybl.iidm.network.Network;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ResultInfos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessStepExecutionContextTest {

    @Mock
    private ProcessExecutionContext<ProcessConfig> processContext;

    @Mock
    private ProcessConfig config;

    @Mock
    private ProcessStepType stepType;

    @Mock
    private Network network;

    @Test
    void shouldInitializeCorrectly() {
        UUID previousStepId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        when(stepType.getName()).thenReturn("test-step");
        when(processContext.getExecutionId()).thenReturn(executionId);
        when(processContext.getNetwork()).thenReturn(network);

        ProcessStepExecutionContext<ProcessConfig> stepContext = new ProcessStepExecutionContext<>(processContext, config, stepType, previousStepId);

        assertThat(stepContext.getStepExecutionId()).isNotNull();
        assertThat(stepContext.getPreviousStepExecutionId()).isEqualTo(previousStepId);
        assertThat(stepContext.getProcessContext()).isEqualTo(processContext);
        assertThat(stepContext.getConfig()).isEqualTo(config);
        assertThat(stepContext.getProcessStepType()).isEqualTo(stepType);
        assertThat(stepContext.getStartedAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(stepContext.getReportInfos()).isNotNull();
        assertThat(stepContext.getReportInfos().reportNode().getMessageKey()).isEqualTo("process.worker.server.stepType");
        assertThat(stepContext.getProcessExecutionId()).isEqualTo(executionId);
        assertThat(stepContext.getNetwork()).isEqualTo(network);
    }

    @Test
    void stepExecutionContextShouldSetAndGetFromParentExecutionContext() {
        UUID previousStepId = UUID.randomUUID();
        when(stepType.getName()).thenReturn("test-step");
        ProcessStepExecutionContext<ProcessConfig> stepContext = new ProcessStepExecutionContext<>(processContext, config, stepType, previousStepId);

        Network newNetwork = mock(Network.class);
        stepContext.setNetwork(newNetwork);
        verify(processContext).setNetwork(newNetwork);

        ResultInfos resultInfos = mock(ResultInfos.class);
        stepContext.setResultInfos(resultInfos);
        assertThat(stepContext.getResultInfos()).isEqualTo(resultInfos);
    }
}
