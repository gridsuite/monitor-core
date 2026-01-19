/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis;

import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcess;
import org.gridsuite.monitor.worker.server.core.ProcessStep;
import org.gridsuite.monitor.worker.server.processes.commons.steps.LoadNetworkStep;
import org.gridsuite.monitor.worker.server.processes.commons.steps.ApplyModificationsStep;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.steps.SecurityAnalysisRunComputationStep;
import org.gridsuite.monitor.worker.server.services.FilterRestService;
import org.gridsuite.monitor.worker.server.services.FilterService;
import org.gridsuite.monitor.worker.server.services.NetworkConversionService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationRestService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationService;
import org.gridsuite.monitor.worker.server.services.NotificationService;
import org.gridsuite.monitor.worker.server.services.SecurityAnalysisService;
import org.gridsuite.monitor.worker.server.services.StepExecutionService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class SecurityAnalysisProcess extends AbstractProcess<SecurityAnalysisConfig> {

    protected final SecurityAnalysisService securityAnalysisService;

    public SecurityAnalysisProcess(
        StepExecutionService<SecurityAnalysisConfig> stepExecutionService,
        NotificationService notificationService,
        NetworkConversionService networkConversionService,
        SecurityAnalysisService securityAnalysisService,
        NetworkModificationService networkModificationService,
        NetworkModificationRestService networkModificationRestService,
        FilterService filterService,
        FilterRestService filterRestService) {
        super(ProcessType.SECURITY_ANALYSIS, stepExecutionService, notificationService, networkConversionService, networkModificationService, networkModificationRestService, filterService, filterRestService);
        this.securityAnalysisService = securityAnalysisService;
    }

    @Override
    protected List<ProcessStep<SecurityAnalysisConfig>> defineSteps() {
        return List.of(
            new LoadNetworkStep<>(networkConversionService),
            new ApplyModificationsStep<>(networkModificationService, networkModificationRestService, filterService),
            new SecurityAnalysisRunComputationStep(securityAnalysisService)
        );
    }
}
