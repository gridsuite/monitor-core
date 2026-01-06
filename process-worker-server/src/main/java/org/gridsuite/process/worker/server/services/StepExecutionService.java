package org.gridsuite.process.worker.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.process.commons.ProcessExecutionStep;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.process.worker.server.core.ProcessStep;

import org.gridsuite.process.commons.StepStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StepExecutionService<C extends ProcessConfig> {

    private final NotificationService notificationService;
    private final ReportService reportService;

    public void skipStep(ProcessStepExecutionContext<?> context, ProcessStep<?> step) {
        context.getProcessContext().setLastExecutedStepId(context.getStepExecutionId());
        ProcessExecutionStep executionStep = new ProcessExecutionStep(
                context.getStepExecutionId(),
                step.getType().getName(),
                step.getPreviousStepId(),
                StepStatus.SKIPPED,
                null,
                null,
                null,
                context.getStartedAt(),
                Instant.now()
        );
        notificationService.updateStepStatus(context.getProcessExecutionId(), executionStep);
    }

    public void executeStep(ProcessStepExecutionContext<C> context, ProcessStep<C> step) {
        context.getProcessContext().setLastExecutedStepId(context.getStepExecutionId());

        ProcessExecutionStep executionStep = new ProcessExecutionStep(
            context.getStepExecutionId(),
            step.getType().getName(),
            step.getPreviousStepId(),
            StepStatus.RUNNING,
            null,
            null,
            null,
            context.getStartedAt(),
            null
        );
        notificationService.updateStepStatus(context.getProcessExecutionId(), executionStep);

        try {
            step.execute(context);
            reportService.sendReport(context.getReportInfos());
            updateStepStatus(context, StepStatus.COMPLETED, step);
        } catch (Exception e) {
            updateStepStatus(context, StepStatus.FAILED, step);
            throw e;
        }
    }

    private void updateStepStatus(ProcessStepExecutionContext<?> context, StepStatus status, ProcessStep<?> step) {
        ProcessExecutionStep updated = new ProcessExecutionStep(
            context.getStepExecutionId(),
            step.getType().getName(),
            step.getPreviousStepId(),
            status,
            context.getResultInfos() != null ? context.getResultInfos().resultUUID() : null,
            context.getResultInfos() != null ? context.getResultInfos().resultType() : null,
            context.getReportInfos().reportUuid(),
            context.getStartedAt(),
            Instant.now()
        );
        notificationService.updateStepStatus(context.getProcessExecutionId(), updated);
    }
}
