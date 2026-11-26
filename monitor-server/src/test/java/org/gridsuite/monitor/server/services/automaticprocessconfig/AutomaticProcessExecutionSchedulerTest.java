/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.server.messaging.NotificationService;
import org.gridsuite.monitor.server.services.processexecution.AutomaticProcessExecutionScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
class AutomaticProcessExecutionSchedulerTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(SchedulerConfiguration.class)
        .withBean(NotificationService.class, () -> mock(NotificationService.class));

    @Test
    void publishesConfiguredCaseUuid() {
        NotificationService notificationService = mock(NotificationService.class);
        UUID caseUuid = UUID.randomUUID();
        AutomaticProcessExecutionScheduler scheduler = new AutomaticProcessExecutionScheduler(notificationService, caseUuid);

        scheduler.publishAutomaticExecution();

        verify(notificationService).sendAutomaticExecutionMessage(caseUuid);
    }

    @Test
    void schedulerIsDisabledWithoutCaseUuid() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AutomaticProcessExecutionScheduler.class));
    }

    @Test
    void schedulerStartsWhenCaseUuidIsConfigured() {
        contextRunner.withPropertyValues("monitor.automatic-execution.case-uuid=" + UUID.randomUUID())
            .run(context -> assertThat(context).hasSingleBean(AutomaticProcessExecutionScheduler.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @Import(AutomaticProcessExecutionScheduler.class)
    static class SchedulerConfiguration {
    }
}
