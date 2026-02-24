/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.commons.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.monitor.commons.api.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.process.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.services.FilterService;
import org.gridsuite.monitor.worker.server.client.NetworkModificationRestClient;
import org.gridsuite.monitor.worker.server.services.NetworkModificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class ApplyModificationsStep<C extends ProcessConfig> extends AbstractProcessStep<C> {

    private final NetworkModificationService networkModificationService;
    private final NetworkModificationRestClient networkModificationRestClient;

    private final FilterService filterService;

    public ApplyModificationsStep(NetworkModificationService networkModificationService, NetworkModificationRestClient networkModificationRestClient,
                                  FilterService filterService) {
        super(CommonStepType.APPLY_MODIFICATIONS);
        this.networkModificationService = networkModificationService;
        this.networkModificationRestClient = networkModificationRestClient;
        this.filterService = filterService;
    }

    @Override
    public void execute(ProcessStepExecutionContext<C> context) {
        List<UUID> modificationIds = context.getConfig().modificationUuids();
        Network network = context.getNetwork();
        if (CollectionUtils.isNotEmpty(modificationIds)) {
            applyModifications(modificationIds, network, context.getReportInfos().reportNode());
        }
    }

    private void applyModifications(List<UUID> modificationIds, Network network, ReportNode reportNode) {
        List<ModificationInfos> modificationInfos = networkModificationRestClient.getModifications(modificationIds);
        networkModificationService.applyModifications(network, modificationInfos, reportNode, filterService);
    }
}
