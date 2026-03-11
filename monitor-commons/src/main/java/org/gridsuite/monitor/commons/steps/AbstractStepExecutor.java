/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.steps;

import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.ReportInfos;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.commons.StepStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Mohamed Ben-rejeb <mohamed.ben-rejeb at rte-france.com>
 */
public abstract class AbstractStepExecutor {

    protected final StepStatusPublisher stepStatusPublisher;
    protected final ReportPublisher reportPublisher;

    protected AbstractStepExecutor(StepStatusPublisher stepStatusPublisher, ReportPublisher reportPublisher) {
        this.stepStatusPublisher = stepStatusPublisher;
        this.reportPublisher = reportPublisher;
    }

    public void skipStep(
            UUID processExecutionId,
            UUID stepExecutionId,
            String stepTypeName,
            int stepOrder,
            Instant startedAt
    ) {
        ProcessExecutionStep executionStep = ProcessExecutionStep.builder()
                .id(stepExecutionId)
                .stepType(stepTypeName)
                .stepOrder(stepOrder)
                .status(StepStatus.SKIPPED)
                .startedAt(startedAt)
                .completedAt(Instant.now())
                .build();
        stepStatusPublisher.updateStepStatus(processExecutionId, executionStep);
    }

    public void executeStep(
            UUID processExecutionId,
            UUID stepExecutionId,
            String stepTypeName,
            int stepOrder,
            Instant startedAt,
            UUID reportUuid,
            ReportInfos reportInfos,
            ResultInfos resultInfos,
            Runnable stepExecution
    ) {
        ProcessExecutionStep executionStep = ProcessExecutionStep.builder()
                .id(stepExecutionId)
                .stepType(stepTypeName)
                .stepOrder(stepOrder)
                .status(StepStatus.RUNNING)
                .reportId(reportUuid)
                .startedAt(startedAt)
                .build();
        stepStatusPublisher.updateStepStatus(processExecutionId, executionStep);

        try {
            stepExecution.run();
            updateStepStatus(
                    processExecutionId,
                    stepExecutionId,
                    stepTypeName,
                    stepOrder,
                    startedAt,
                    reportUuid,
                    resultInfos,
                    StepStatus.COMPLETED);
        } catch (Exception e) {
            updateStepStatus(
                    processExecutionId,
                    stepExecutionId,
                    stepTypeName,
                    stepOrder,
                    startedAt,
                    reportUuid,
                    resultInfos,
                    StepStatus.FAILED);
            throw e;
        } finally {
            reportPublisher.sendReport(reportInfos);
        }
    }

    private void updateStepStatus(
            UUID processExecutionId,
            UUID stepExecutionId,
            String stepTypeName,
            int stepOrder,
            Instant startedAt,
            UUID reportUuid,
            ResultInfos resultInfos,
            StepStatus status
    ) {
        ProcessExecutionStep updated = ProcessExecutionStep.builder()
                .id(stepExecutionId)
                .stepType(stepTypeName)
                .stepOrder(stepOrder)
                .status(status)
                .resultId(resultInfos != null ? resultInfos.resultUUID() : null)
                .resultType(resultInfos != null ? resultInfos.resultType() : null)
                .reportId(reportUuid)
                .startedAt(startedAt)
                .completedAt(Instant.now())
                .build();
        stepStatusPublisher.updateStepStatus(processExecutionId, updated);
    }
}
