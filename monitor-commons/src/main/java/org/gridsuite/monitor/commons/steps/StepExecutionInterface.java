/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.steps;

import org.gridsuite.monitor.commons.ProcessExecutionStep;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.commons.StepStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Mohamed Ben-rejeb <mohamed.ben-rejeb at rte-france.com>
 */
public interface StepExecutionInterface<R> {

    UUID getExecutionId();

    void setExecutionId(UUID executionId);

    StepStatusPublisher getStepStatusPublisher();

    ReportPublisher<R> getReportPublisher();

    default void skipStep(UUID stepExecutionId, String stepTypeName, int stepOrder, Instant startedAt) {
        ProcessExecutionStep executionStep = new ProcessExecutionStep(
                stepExecutionId,
                stepTypeName,
                stepOrder,
                StepStatus.SKIPPED,
                null,
                null,
                null,
                startedAt,
                Instant.now()
        );
        getStepStatusPublisher().updateStepStatus(getExecutionId(), executionStep);
    }

    default void executeStep(UUID stepExecutionId,
                             String stepTypeName,
                             int stepOrder,
                             Instant startedAt,
                             UUID reportUuid,
                             R reportInfos,
                             ResultInfos resultInfos,
                             Runnable stepExecution) {
        ProcessExecutionStep executionStep = new ProcessExecutionStep(
                stepExecutionId,
                stepTypeName,
                stepOrder,
                StepStatus.RUNNING,
                null,
                null,
                reportUuid,
                startedAt,
                null
        );
        getStepStatusPublisher().updateStepStatus(getExecutionId(), executionStep);

        try {
            stepExecution.run();
            getReportPublisher().sendReport(reportInfos);
            updateStepStatus(stepExecutionId, stepTypeName, stepOrder, startedAt, reportUuid, resultInfos, StepStatus.COMPLETED);
        } catch (Exception e) {
            updateStepStatus(stepExecutionId, stepTypeName, stepOrder, startedAt, reportUuid, resultInfos, StepStatus.FAILED);
            throw e;
        }
    }

    default void updateStepStatus(UUID stepExecutionId,
                                  String stepTypeName,
                                  int stepOrder,
                                  Instant startedAt,
                                  UUID reportUuid,
                                  ResultInfos resultInfos,
                                  StepStatus status) {
        ProcessExecutionStep updated = new ProcessExecutionStep(
                stepExecutionId,
                stepTypeName,
                stepOrder,
                status,
                resultInfos != null ? resultInfos.resultUUID() : null,
                resultInfos != null ? resultInfos.resultType() : null,
                reportUuid,
                startedAt,
                Instant.now()
        );
        getStepStatusPublisher().updateStepStatus(getExecutionId(), updated);
    }
}
