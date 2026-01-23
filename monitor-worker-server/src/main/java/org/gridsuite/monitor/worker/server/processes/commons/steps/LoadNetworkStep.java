/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.commons.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.services.NetworkConversionService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class LoadNetworkStep<C extends ProcessConfig> extends AbstractProcessStep<C> {

    private final NetworkConversionService networkConversionService;

    public LoadNetworkStep(NetworkConversionService networkConversionService) {
        super(CommonStepType.LOAD_NETWORK);
        this.networkConversionService = networkConversionService;
    }

    @Override
    public void execute(ProcessStepExecutionContext<C> context) {
        UUID caseId = context.getCaseUuid();
        Network network = loadNetworkFromCase(caseId, context.getReportInfos().reportNode());
        context.setNetwork(network);
    }

    private Network loadNetworkFromCase(UUID caseUuid, ReportNode reportNode) {
        ReportNode reporter = reportNode.newReportNode()
                    .withMessageTemplate("monitor.worker.server.importCase")
                    .withUntypedValue("caseUuid", caseUuid.toString())
                    .add();
        return networkConversionService.createNetwork(caseUuid, reporter);
    }
}
