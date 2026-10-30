/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.StepStatus;
import org.gridsuite.monitor.worker.server.clients.ReportRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.context.StepWithContext;
import org.gridsuite.monitor.worker.server.core.exceptions.ProcessCancelledException;
import org.gridsuite.monitor.worker.server.core.messaging.Notificator;
import org.gridsuite.monitor.worker.server.core.orchestrator.StepExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.concurrent.CancellationException;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
@RequiredArgsConstructor
public class StepExecutionService implements StepExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(StepExecutionService.class);

    private final Notificator notificationService;
    private final ReportRestClient reportRestClient;

    private void handleException(ReportNode reportNode, Exception e) {
        boolean isApplicationException = PowsyblException.class.isAssignableFrom(e.getClass());
        if (!isApplicationException && LOGGER.isErrorEnabled()) {
            LOGGER.error(e.toString(), e);
        }
        String errorMessage = isApplicationException ? e.getMessage() : "Technical error: " + e;

        reportNode.newReportNode()
            .withMessageTemplate("monitor.worker.server.step.error")
            .withUntypedValue("errorMessage", errorMessage)
            .withSeverity(TypedValue.ERROR_SEVERITY)
            .add();
    }

    @Override
    public <C extends ProcessConfig> void executeStep(StepWithContext<C> stepWithContext) {
        ProcessStepExecutionContext<C> context = stepWithContext.stepExecutionContext();
        StepStatus status = StepStatus.COMPLETED;

        try {
            initializeStep(context);
            stepWithContext.step().execute(context);
        } catch (CancellationException e) {
            status = StepStatus.CANCELED;
            throw new ProcessCancelledException("Execution canceled", e);
        } catch (ProcessCancelledException e) {
            status = StepStatus.CANCELED;
            throw e;
        } catch (Exception e) {
            status = StepStatus.FAILED;
            handleException(context.getReportNode(), e);
            throw e;
        } finally {
            sendReportSafely(context);
            updateStepStatus(context, status);
        }
    }

    private <C extends ProcessConfig> void initializeStep(ProcessStepExecutionContext<C> context) {
        context.setStartedAt(Instant.now());
        updateStepStatus(context, StepStatus.RUNNING);
    }

    private <C extends ProcessConfig> void updateStepStatus(ProcessStepExecutionContext<C> context, StepStatus status) {
        ProcessExecutionStep updatedStep = ProcessExecutionStep.builder()
                .id(context.getStepExecutionId())
                .stepType(context.getProcessStepType().getName())
                .stepOrder(context.getStepOrder())
                .status(status)
                .resultId(context.getResultInfos() != null ? context.getResultInfos().resultUUID() : null)
                .resultType(context.getResultInfos() != null ? context.getResultInfos().resultType() : null)
                .startedAt(context.getStartedAt())
                .completedAt(status == StepStatus.COMPLETED || status == StepStatus.FAILED || status == StepStatus.CANCELED ? Instant.now() : null)
                .build();

        notificationService.updateStepStatus(context.getProcessExecutionId(), context.getConfig().processType(), updatedStep);
    }

    private <C extends ProcessConfig> void sendReportSafely(ProcessStepExecutionContext<C> context) {
        try {
            reportRestClient.sendReportChildren(context.getProcessReportId(), context.getReportNode());
        } catch (Exception e) {
            LOGGER.error("Execution with id: {} failed at step: {} - {}", context.getProcessExecutionId(), context.getProcessStepType().getName(), e.getMessage());
        }
    }
}
