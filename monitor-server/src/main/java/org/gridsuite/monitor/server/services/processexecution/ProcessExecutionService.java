/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.monitor.commons.types.messaging.ProcessExecutionStep;
import org.gridsuite.monitor.commons.types.processexecution.ProcessStatus;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.clients.ReportRestClient;
import org.gridsuite.monitor.server.clients.S3RestClient;
import org.gridsuite.monitor.server.dto.processexecution.ProcessExecution;
import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.gridsuite.monitor.server.messaging.NotificationService;
import org.gridsuite.monitor.server.services.result.ResultService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Service
public class ProcessExecutionService {

    private final ProcessExecutionTxService processExecutionTxService;
    private final NotificationService notificationService;
    private final ReportRestClient reportRestClient;
    private final ResultService resultService;
    private final S3RestClient s3RestClient;

    public ProcessExecutionService(ProcessExecutionTxService processExecutionTxService,
                                   NotificationService notificationService,
                                   ReportRestClient reportRestClient,
                                   ResultService resultService,
                                   S3RestClient s3RestClient) {
        this.processExecutionTxService = processExecutionTxService;
        this.notificationService = notificationService;
        this.reportRestClient = reportRestClient;
        this.resultService = resultService;
        this.s3RestClient = s3RestClient;
    }

    public UUID executeProcess(UUID caseUuid, String userId, UUID processConfigId, boolean isDebug) {
        UUID executionId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();

        ProcessCreationResult result = processExecutionTxService.createExecution(
            caseUuid, userId, processConfigId, executionId, reportId, isDebug
        );

        notificationService.sendProcessRunMessage(
            caseUuid,
            result.processConfig(),
            executionId,
            reportId,
            result.debugLocationFile()
        );

        notificationService.sendProcessUpdatedMessage(
            result.processConfig().processType(),
            executionId
        );

        return executionId;
    }

    public void updateExecutionStatus(UUID executionId, ProcessStatus status, String executionEnvName, Instant startedAt, Instant completedAt) {
        processExecutionTxService.updateExecutionStatus(executionId, status, executionEnvName, startedAt, completedAt);
    }

    public void updateStepStatus(UUID executionId, ProcessExecutionStep processExecutionStep) {
        processExecutionTxService.updateStepStatus(executionId, processExecutionStep);
    }

    public void updateStepsStatuses(UUID executionId, List<ProcessExecutionStep> processExecutionSteps) {
        processExecutionTxService.updateStepsStatuses(executionId, processExecutionSteps);
    }

    public ReportPage getReports(UUID executionId) {
        return reportRestClient.getReport(processExecutionTxService.getReportId(executionId));
    }

    public List<String> getResults(UUID executionId) {
        return processExecutionTxService.getResultInfos(executionId).stream()
            .map(resultService::getResult)
            .toList();
    }

    public byte[] getDebugInfos(UUID executionId) {
        String debugFileLocation = processExecutionTxService.getDebugFileLocation(executionId);
        try {
            return s3RestClient.downloadDirectoryAsZip(debugFileLocation);
        } catch (IOException e) {
            throw new PowsyblException("An error occurred while downloading debug files", e);
        }
    }

    public List<ProcessExecution> getLaunchedProcesses(ProcessType processType) {
        return processExecutionTxService.getLaunchedProcesses(processType);
    }

    public List<ProcessExecutionStep> getStepsInfos(UUID executionId) {
        return processExecutionTxService.getStepsInfos(executionId);
    }

    public void deleteExecution(UUID executionId) {
        ProcessDeletionInfos executionDeletionData = processExecutionTxService.deleteExecution(executionId);
        executionDeletionData.resultInfos().forEach(resultService::deleteResult);
        if (executionDeletionData.reportId() != null) {
            reportRestClient.deleteReport(executionDeletionData.reportId());
        }
    }
}
