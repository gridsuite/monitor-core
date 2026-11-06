/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processexecution;

import org.gridsuite.monitor.server.messaging.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@Component
@ConditionalOnProperty(prefix = "monitor.automatic-execution", name = "case-uuid")
public class AutomaticProcessExecutionScheduler {
    private final NotificationService notificationService;
    private final UUID caseUuid;

    public AutomaticProcessExecutionScheduler(
        NotificationService notificationService,
        @Value("${monitor.automatic-execution.case-uuid}") UUID caseUuid
    ) {
        this.notificationService = notificationService;
        this.caseUuid = caseUuid;
    }

    @Scheduled(fixedRateString = "${monitor.automatic-execution.interval:PT30M}")
    public void publishAutomaticExecution() {
        notificationService.sendAutomaticExecutionMessage(caseUuid);
    }
}
