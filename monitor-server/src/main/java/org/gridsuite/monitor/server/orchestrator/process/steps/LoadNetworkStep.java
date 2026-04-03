/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator.process.steps;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.clients.CaseServerRestClient;
import org.gridsuite.monitor.server.orchestrator.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.server.orchestrator.process.AbstractProcessStep;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Step that loads a network case from case-server and stores it in S3.
 * The S3 key is saved in the execution context for use by subsequent steps.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class LoadNetworkStep<C extends ProcessConfig> extends AbstractProcessStep<C> {

    private final CaseServerRestClient caseServerRestClient;

    public LoadNetworkStep(CaseServerRestClient caseServerRestClient) {
        super(CommonStepType.LOAD_NETWORK);
        this.caseServerRestClient = caseServerRestClient;
    }

    @Override
    public void execute(ProcessStepExecutionContext<C> context) {
        UUID caseUuid = context.getCaseUuid();
        // TODO: call case-server to export the case to S3 and get the S3 key
        String s3Key = caseServerRestClient.exportCaseToS3(caseUuid);
        context.setCaseS3Key(s3Key);
    }
}
