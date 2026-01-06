package org.gridsuite.process.worker.server.core;

import lombok.Getter;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;
import org.gridsuite.process.worker.server.services.*;

import java.util.List;
import java.util.UUID;

@Getter
public abstract class AbstractProcess<C extends ProcessConfig> implements Process<C> {

    protected final ProcessType processType;
    protected final StepExecutionService<C> stepExecutionService;
    protected final NotificationService notificationService;
    protected final NetworkConversionService networkConversionService;

    protected AbstractProcess(
            ProcessType processType,
            StepExecutionService<C> stepExecutionService,
            NotificationService notificationService,
            NetworkConversionService networkConversionService) {
        this.processType = processType;
        this.stepExecutionService = stepExecutionService;
        this.notificationService = notificationService;
        this.networkConversionService = networkConversionService;
    }

    @Override
    public void execute(ProcessExecutionContext<C> context) {
        List<ProcessStep<C>> steps = defineSteps();
        boolean skipRemaining = false;
        UUID lastStepId = null;
        for (ProcessStep<C> step : steps) {
            step.setPreviousStepId(lastStepId);
            if (skipRemaining) {
                skipStep(context, step);
            } else {
                try {
                    executeStep(context, step);
                } catch (Exception e) {
                    handleStepFailure(context, step, e);
                    skipRemaining = true;
                }
            }
            lastStepId = context.getLastExecutedStepId();
        }
    }

    protected abstract List<ProcessStep<C>> defineSteps();

    protected void executeStep(ProcessExecutionContext<C> context, ProcessStep<C> step) {
        ProcessStepExecutionContext<C> stepContext = context.createStepContext(step);
        stepExecutionService.executeStep(stepContext, step);
    }

    protected void skipStep(ProcessExecutionContext<C> context, ProcessStep<C> step) {
        ProcessStepExecutionContext<C> stepContext = context.createStepContext(step);
        stepExecutionService.skipStep(stepContext, step);
    }

    protected void handleStepFailure(ProcessExecutionContext<C> context, ProcessStep<C> step, Exception e) {
        //TODO better error handling
        e.printStackTrace();
    }
}
