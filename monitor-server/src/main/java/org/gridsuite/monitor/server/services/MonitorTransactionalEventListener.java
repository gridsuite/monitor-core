/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
@RequiredArgsConstructor
public class MonitorTransactionalEventListener {

    private final NotificationService notificationService;
    private final ResultService resultService;
    private final ReportService reportService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProcessRunRequested(MonitorTransactionalEvents.ProcessRunRequested event) {
        notificationService.sendProcessRunMessage(
            event.caseUuid(),
            event.processConfig(),
            event.executionId(),
            event.debugFileLocation()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExecutionDeleted(MonitorTransactionalEvents.ExecutionDeleted event) {
        event.resultInfos().forEach(resultService::deleteResult);
        event.reportIds().forEach(reportService::deleteReport);
    }
}
