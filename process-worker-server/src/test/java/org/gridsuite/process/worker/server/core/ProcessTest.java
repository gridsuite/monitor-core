package org.gridsuite.process.worker.server.core;

import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.processes.securityanalysis.DummySecurityAnalysisService;
import org.gridsuite.process.worker.server.processes.securityanalysis.SecurityAnalysisProcess;
import org.gridsuite.process.worker.server.services.NetworkConversionService;
import org.gridsuite.process.worker.server.services.NotificationService;
import org.gridsuite.process.worker.server.services.StepExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessTest {
    @Mock
    private StepExecutionService<SecurityAnalysisConfig> stepExecutionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NetworkConversionService networkConversionService;

    @Mock
    private DummySecurityAnalysisService securityAnalysisService;

    @Mock
    private ProcessExecutionContext<SecurityAnalysisConfig> processContext;

    @Captor
    private ArgumentCaptor<UUID> previousStepIdCaptor;

    private SecurityAnalysisProcess process;

    @BeforeEach
    void setUp() {
        process = new SecurityAnalysisProcess(
                stepExecutionService,
                notificationService,
                networkConversionService,
                securityAnalysisService
        );
    }

    @Test
    void executeShouldExecuteAllStepsSuccessfullyWhenNoErrors() {
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();
        UUID step3Id = UUID.randomUUID();

        ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext1 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext2 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext3 = mock(ProcessStepExecutionContext.class);

        when(stepContext1.getStepExecutionId()).thenReturn(step1Id);
        when(stepContext2.getStepExecutionId()).thenReturn(step2Id);
        when(stepContext3.getStepExecutionId()).thenReturn(step3Id);

        when(processContext.createStepContext(any(), any()))
                .thenReturn(stepContext1)
                .thenReturn(stepContext2)
                .thenReturn(stepContext3);

        process.execute(processContext);

        verify(stepExecutionService, times(3)).executeStep(any(), any());
        verify(stepExecutionService, never()).skipStep(any(), any());
        verify(processContext, times(3)).createStepContext(any(), previousStepIdCaptor.capture());
        // Verify previousStepId is correctly set
        InOrder inOrder = inOrder(processContext);
        inOrder.verify(processContext).createStepContext(any(), eq(null));
        inOrder.verify(processContext).createStepContext(any(), eq(step1Id));
        inOrder.verify(processContext).createStepContext(any(), eq(step2Id));
    }

    @Test
    void executeShouldSkipAllRemainingStepsWhenFirstStepFails() {
        UUID step1Id = UUID.randomUUID();
        UUID step2Id = UUID.randomUUID();
        UUID step3Id = UUID.randomUUID();
        ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext1 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext2 = mock(ProcessStepExecutionContext.class);
        ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext3 = mock(ProcessStepExecutionContext.class);
        when(stepContext1.getStepExecutionId()).thenReturn(step1Id);
        when(stepContext2.getStepExecutionId()).thenReturn(step2Id);
        when(stepContext3.getStepExecutionId()).thenReturn(step3Id);
        when(processContext.createStepContext(any(), any()))
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
        verify(processContext, times(3)).createStepContext(any(), previousStepIdCaptor.capture());
        // Verify previousStepId is correctly set
        InOrder inOrder = inOrder(processContext);
        inOrder.verify(processContext).createStepContext(any(), eq(null));
        inOrder.verify(processContext).createStepContext(any(), eq(step1Id));
        inOrder.verify(processContext).createStepContext(any(), eq(step2Id));
    }
}
