/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.core;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.worker.server.services.internal.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessTest {
    @Mock
    private StepExecutionService<ProcessConfig> stepExecutionService;

    @Mock
    private ProcessExecutionContext<ProcessConfig> processContext;

    private TestProcess process;

    @BeforeEach
    void setUp() {
        process = new TestProcess(
                stepExecutionService);
    }

    @Test
    void executeShouldExecuteAllStepsSuccessfullyWhenNoErrors() {
        ProcessStepExecutionContext<ProcessConfig> stepContext1 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext2 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext3 = mock(ProcessStepExecutionContext.class);

        when(processContext.createStepContext(any(), anyInt()))
                .thenReturn(stepContext1)
                .thenReturn(stepContext2)
                .thenReturn(stepContext3);

        process.execute(processContext);

        verify(stepExecutionService, times(3)).executeStep(any(), any());
        verify(stepExecutionService, never()).skipStep(any(), any());
        // Verify stepOrder is correctly set
        InOrder inOrder = inOrder(processContext);
        inOrder.verify(processContext).createStepContext(any(), eq(0));
        inOrder.verify(processContext).createStepContext(any(), eq(1));
        inOrder.verify(processContext).createStepContext(any(), eq(2));
    }

    @Test
    void executeShouldSkipAllRemainingStepsWhenFirstStepFails() {
        ProcessStepExecutionContext<ProcessConfig> stepContext1 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext2 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<ProcessConfig> stepContext3 = mock(ProcessStepExecutionContext.class);
        when(processContext.createStepContext(any(), anyInt()))
                .thenReturn(stepContext1)
                .thenReturn(stepContext2)
                .thenReturn(stepContext3);
        // Make the first step fail
        doThrow(new RuntimeException("Network loading failed"))
                .when(stepExecutionService).executeStep(eq(stepContext1), any());

        process.execute(processContext);

        verify(stepExecutionService).executeStep(eq(stepContext1), any());
        verify(stepExecutionService).skipStep(eq(stepContext2), any());
        verify(stepExecutionService).skipStep(eq(stepContext3), any());
        verify(stepExecutionService, times(1)).executeStep(any(), any());
        verify(stepExecutionService, times(2)).skipStep(any(), any());
        // Verify stepOrder is correctly set
        InOrder inOrder = inOrder(processContext);
        inOrder.verify(processContext).createStepContext(any(), eq(0));
        inOrder.verify(processContext).createStepContext(any(), eq(1));
        inOrder.verify(processContext).createStepContext(any(), eq(2));
    }

    /**
     * Mock implementation of AbstractProcess for testing purposes
     */
    private static class TestProcess extends AbstractProcess<ProcessConfig> {

        public TestProcess(StepExecutionService<ProcessConfig> stepExecutionService) {
            super(ProcessType.SECURITY_ANALYSIS, stepExecutionService);
        }

        @Override
        public List<ProcessStep<ProcessConfig>> defineSteps() {
            // Return 3 dummy steps for testing
            ProcessStep<ProcessConfig> step1 = mock(ProcessStep.class);
            ProcessStep<ProcessConfig> step2 = mock(ProcessStep.class);
            ProcessStep<ProcessConfig> step3 = mock(ProcessStep.class);

            return List.of(step1, step2, step3);
        }
    }
}
