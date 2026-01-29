/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.events;

import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.server.services.NotificationService;
import org.gridsuite.monitor.server.services.ReportService;
import org.gridsuite.monitor.server.services.ResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
@RequiredArgsConstructor
public class MonitorEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorEventListener.class);
    private final NotificationService notificationService;
    private final ResultService resultService;
    private final ReportService reportService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProcessScheduled(ProcessExecutionEvent event) {
        notificationService.sendProcessRunMessage(event.caseUuid(), event.config(), event.executionId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExecutionDeleted(ExecutionDeletedEvent event) {
        event.resultInfos().forEach(this::safeDeleteResult);
        event.reportIds().forEach(this::safeDeleteReport);
    }

    private void safeDeleteResult(ResultInfos resultInfos) {
        try {
            resultService.deleteResult(resultInfos);
        } catch (Exception e) {
            LOGGER.error("Failed to delete result {} of type {}: {}",
                resultInfos.resultUUID(), resultInfos.resultType(), e.getMessage(), e);
        }
    }

    private void safeDeleteReport(UUID reportId) {
        try {
            reportService.deleteReport(reportId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete report {}: {}", reportId, e.getMessage(), e);
        }
    }
}
