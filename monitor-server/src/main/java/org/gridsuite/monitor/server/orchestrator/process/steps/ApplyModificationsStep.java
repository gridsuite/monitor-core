/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator.process.steps;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.clients.NetworkModificationRestClient;
import org.gridsuite.monitor.server.orchestrator.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.server.orchestrator.process.AbstractProcessStep;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Step that applies network modifications by delegating to the network-modification-server.
 * The modifications are applied on the network stored in S3 (referenced by the context's S3 key).
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class ApplyModificationsStep<C extends ProcessConfig> extends AbstractProcessStep<C> {

    private final NetworkModificationRestClient networkModificationRestClient;

    public ApplyModificationsStep(NetworkModificationRestClient networkModificationRestClient) {
        super(CommonStepType.APPLY_MODIFICATIONS);
        this.networkModificationRestClient = networkModificationRestClient;
    }

    @Override
    public void execute(ProcessStepExecutionContext<C> context) {
        List<UUID> modificationIds = context.getConfig().modificationUuids();
        if (modificationIds != null && !modificationIds.isEmpty()) {
            // TODO: call network-modification-server to apply modifications on the network in S3
            String updatedS3Key = networkModificationRestClient.applyModifications(
                context.getCaseS3Key(),
                modificationIds
            );
            context.setCaseS3Key(updatedS3Key);
        }
    }
}
