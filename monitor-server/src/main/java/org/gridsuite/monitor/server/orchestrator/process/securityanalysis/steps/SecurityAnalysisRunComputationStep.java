/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator.process.securityanalysis.steps;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.server.orchestrator.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.server.orchestrator.process.AbstractProcessStep;
import org.gridsuite.monitor.server.orchestrator.process.securityanalysis.SecurityAnalysisStepType;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Step that runs a security analysis by delegating to the security-analysis-server.
 * The network is read from S3 by the SA server. This step polls for the result.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class SecurityAnalysisRunComputationStep extends AbstractProcessStep<SecurityAnalysisConfig> {

    private final SecurityAnalysisRestClient securityAnalysisRestClient;

    public SecurityAnalysisRunComputationStep(SecurityAnalysisRestClient securityAnalysisRestClient) {
        super(SecurityAnalysisStepType.RUN_SA_COMPUTATION);
        this.securityAnalysisRestClient = securityAnalysisRestClient;
    }

    @Override
    public void execute(ProcessStepExecutionContext<SecurityAnalysisConfig> context) {
        String caseS3Key = context.getCaseS3Key();

        // TODO: call security-analysis-server's run endpoint
        // Expected API: POST /security-analysis/run
        //   Body: { "caseS3Key": "...", "securityAnalysisParametersUuid": "...", "loadflowParametersUuid": "..." }
        //   Returns: result UUID
        UUID resultUuid = securityAnalysisRestClient.run(
            caseS3Key,
            context.getConfig().securityAnalysisParametersUuid(),
            context.getConfig().loadflowParametersUuid()
        );

        ResultInfos resultInfos = new ResultInfos(resultUuid, ResultType.SECURITY_ANALYSIS);
        context.setResultInfos(resultInfos);
    }
}
