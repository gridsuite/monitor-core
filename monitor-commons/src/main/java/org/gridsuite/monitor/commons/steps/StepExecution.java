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
public interface StepExecution {

    StepStatusPublisher getStepStatusPublisher();

    ReportPublisher getReportPublisher();

    default void skipStep(
            UUID processExecutionId,
            UUID stepExecutionId,
            String stepTypeName,
            int stepOrder,
            Instant startedAt
    ) {
        ProcessExecutionStep executionStep = new ProcessExecutionStep(
                stepExecutionId,
                stepTypeName,
                stepOrder,
                StepStatus.SKIPPED,
                null,
                null,
                null,
                startedAt,
                Instant.now());
        getStepStatusPublisher().updateStepStatus(processExecutionId, executionStep);
    }

    default void executeStep(
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
        ProcessExecutionStep executionStep = new ProcessExecutionStep(
                stepExecutionId,
                stepTypeName,
                stepOrder,
                StepStatus.RUNNING,
                null,
                null,
                reportUuid,
                startedAt,
                null);
        getStepStatusPublisher().updateStepStatus(processExecutionId, executionStep);

        try {
            stepExecution.run();
            getReportPublisher().sendReport(reportInfos);
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
        }
    }

    default void updateStepStatus(
            UUID processExecutionId,
            UUID stepExecutionId,
            String stepTypeName,
            int stepOrder,
            Instant startedAt,
            UUID reportUuid,
            ResultInfos resultInfos,
            StepStatus status
    ) {
        ProcessExecutionStep updated = new ProcessExecutionStep(
                stepExecutionId,
                stepTypeName,
                stepOrder,
                status,
                resultInfos != null ? resultInfos.resultUUID() : null,
                resultInfos != null ? resultInfos.resultType() : null,
                reportUuid,
                startedAt,
                Instant.now());
        getStepStatusPublisher().updateStepStatus(processExecutionId, updated);
    }
}
