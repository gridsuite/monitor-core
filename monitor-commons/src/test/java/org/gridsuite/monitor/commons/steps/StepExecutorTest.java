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
import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.commons.StepStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mohamed Ben-rejeb <mohamed.ben-rejeb at rte-france.com>
 */
class StepExecutorTest {

    @Test
    void skipStepShouldPublishSkippedStatus() {
        TestStepExecutor executor = new TestStepExecutor();
        UUID processExecutionId = UUID.randomUUID();
        UUID stepExecutionId = UUID.randomUUID();
        Instant startedAt = Instant.now();

        executor.skipStep(processExecutionId, stepExecutionId, "LOAD_NETWORK", 1, startedAt);

        assertEquals(
                1,
                executor.publishedSteps()
                        .size());
        PublishedStep published = executor.publishedSteps()
                .get(0);
        assertEquals(processExecutionId, published.processExecutionId());
        assertEquals(
                stepExecutionId,
                published.step()
                        .getId());
        assertEquals(
                "LOAD_NETWORK",
                published.step()
                        .getStepType());
        assertEquals(
                1,
                published.step()
                        .getStepOrder());
        assertEquals(
                StepStatus.SKIPPED,
                published.step()
                        .getStatus());
        assertNull(published.step()
                .getResultId());
        assertNull(published.step()
                .getResultType());
        assertNull(published.step()
                .getReportId());
        assertEquals(
                startedAt,
                published.step()
                        .getStartedAt());
        assertNotNull(published.step()
                .getCompletedAt());
        assertTrue(executor.publishedReports()
                .isEmpty());
    }

    @Test
    void executeStepShouldPublishRunningThenCompletedAndSendReport() {
        TestStepExecutor executor = new TestStepExecutor();
        UUID processExecutionId = UUID.randomUUID();
        UUID stepExecutionId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        Instant startedAt = Instant.now();

        ResultInfos resultInfos = new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS);
        ReportInfos reportInfos = new ReportInfos(reportId, null);
        AtomicInteger executionCount = new AtomicInteger(0);

        executor.executeStep(
                processExecutionId,
                stepExecutionId,
                "RUN_SECURITY_ANALYSIS",
                2,
                startedAt,
                reportId,
                reportInfos,
                resultInfos,
                executionCount::incrementAndGet);

        assertEquals(1, executionCount.get());
        assertEquals(List.of(reportInfos), executor.publishedReports());
        assertEquals(
                2,
                executor.publishedSteps()
                        .size());

        PublishedStep running = executor.publishedSteps()
                .get(0);
        assertEquals(processExecutionId, running.processExecutionId());
        assertEquals(
                StepStatus.RUNNING,
                running.step()
                        .getStatus());
        assertEquals(
                reportId,
                running.step()
                        .getReportId());
        assertNull(running.step()
                .getCompletedAt());

        PublishedStep completed = executor.publishedSteps()
                .get(1);
        assertEquals(processExecutionId, completed.processExecutionId());
        assertEquals(
                StepStatus.COMPLETED,
                completed.step()
                        .getStatus());
        assertEquals(
                resultId,
                completed.step()
                        .getResultId());
        assertEquals(
                ResultType.SECURITY_ANALYSIS,
                completed.step()
                        .getResultType());
        assertEquals(
                reportId,
                completed.step()
                        .getReportId());
        assertNotNull(completed.step()
                .getCompletedAt());
    }

    @Test
    void executeStepShouldPublishFailedStatusAndRethrowWhenStepThrows() {
        TestStepExecutor executor = new TestStepExecutor();
        UUID processExecutionId = UUID.randomUUID();
        UUID stepExecutionId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Instant startedAt = Instant.now();

        ReportInfos reportInfos = new ReportInfos(reportId, null);
        RuntimeException failure = new RuntimeException("exp");

        Runnable failingExecution = () -> {
            throw failure;
        };

        RuntimeException thrown = assertThrows(
                RuntimeException.class, () -> executeFailingStep(
                        executor,
                        processExecutionId,
                        stepExecutionId,
                        startedAt,
                        reportId,
                        reportInfos,
                        failingExecution));

        assertSame(failure, thrown);
        assertEquals(List.of(reportInfos), executor.publishedReports());
        assertEquals(
                2,
                executor.publishedSteps()
                        .size());

        PublishedStep running = executor.publishedSteps()
                .get(0);
        assertEquals(
                StepStatus.RUNNING,
                running.step()
                        .getStatus());

        PublishedStep failed = executor.publishedSteps()
                .get(1);
        assertEquals(
                StepStatus.FAILED,
                failed.step()
                        .getStatus());
        assertNull(failed.step()
                .getResultId());
        assertNull(failed.step()
                .getResultType());
        assertNotNull(failed.step()
                .getCompletedAt());
    }

    private static final class TestStepExecutor extends AbstractStepExecutor {

        private final List<PublishedStep> publishedSteps;
        private final List<ReportInfos> publishedReports;

        private TestStepExecutor() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        private TestStepExecutor(List<PublishedStep> publishedSteps, List<ReportInfos> publishedReports) {
            super(
                    (executionId, processExecutionStep) -> publishedSteps.add(new PublishedStep(
                            executionId,
                            processExecutionStep)), publishedReports::add);
            this.publishedSteps = publishedSteps;
            this.publishedReports = publishedReports;
        }

        List<PublishedStep> publishedSteps() {
            return publishedSteps;
        }

        List<ReportInfos> publishedReports() {
            return publishedReports;
        }
    }

    private static void executeFailingStep(
            TestStepExecutor executor,
            UUID processExecutionId,
            UUID stepExecutionId,
            Instant startedAt,
            UUID reportId,
            ReportInfos reportInfos,
            Runnable failingExecution
    ) {
        executor.executeStep(
                processExecutionId,
                stepExecutionId,
                "FAILING_STEP",
                3,
                startedAt,
                reportId,
                reportInfos,
                null,
                failingExecution);
    }

    private record PublishedStep(UUID processExecutionId, ProcessExecutionStep step) { }
}
