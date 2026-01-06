package org.gridsuite.process.worker.server.processes.securityanalysis;

import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.worker.server.core.AbstractProcess;
import org.gridsuite.process.worker.server.core.ProcessStep;
import org.gridsuite.process.worker.server.processes.commons.steps.LoadNetworkStep;
import org.gridsuite.process.worker.server.processes.securityanalysis.steps.SAApplyModificationsStep;
import org.gridsuite.process.worker.server.processes.securityanalysis.steps.SARunComputationStep;
import org.gridsuite.process.worker.server.services.NetworkConversionService;
import org.gridsuite.process.worker.server.services.NotificationService;
import org.gridsuite.process.worker.server.services.StepExecutionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityAnalysisProcess extends AbstractProcess<SecurityAnalysisConfig> {

    protected final DummySecurityAnalysisService securityAnalysisService;

    public SecurityAnalysisProcess(
            StepExecutionService<SecurityAnalysisConfig> stepExecutionService,
            NotificationService notificationService,
            NetworkConversionService networkConversionService,
            DummySecurityAnalysisService securityAnalysisService) {
        super(ProcessType.SECURITY_ANALYSIS, stepExecutionService, notificationService, networkConversionService);
        this.securityAnalysisService = securityAnalysisService;
    }

    @Override
    protected List<ProcessStep<SecurityAnalysisConfig>> defineSteps() {
        return List.of(
            new LoadNetworkStep<>(networkConversionService),
            new SAApplyModificationsStep(),
            new SARunComputationStep(securityAnalysisService)
        );
    }
}
