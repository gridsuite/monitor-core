/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.orchestrator.process.securityanalysis.steps;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.server.orchestrator.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.server.orchestrator.process.AbstractProcessStep;
import org.gridsuite.monitor.server.orchestrator.process.securityanalysis.SecurityAnalysisStepType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Async step that runs a security analysis by delegating to the security-analysis-server.
 * <p>
 * This step fires a request to SA-server and returns immediately.
 * SA-server will send a RabbitMQ callback when the computation completes,
 * including the receiver headers passed in the request.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class SecurityAnalysisRunComputationStep extends AbstractProcessStep<SecurityAnalysisConfig> {

    private final SecurityAnalysisRestClient securityAnalysisRestClient;
    private final String stepResultDestination;

    public SecurityAnalysisRunComputationStep(SecurityAnalysisRestClient securityAnalysisRestClient,
                                             @Value("${monitor.step-result.destination:monitor.step.result}") String stepResultDestination) {
        super(SecurityAnalysisStepType.RUN_SA_COMPUTATION);
        this.securityAnalysisRestClient = securityAnalysisRestClient;
        this.stepResultDestination = stepResultDestination;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void execute(ProcessStepExecutionContext<SecurityAnalysisConfig> context) {
        UUID resultUuid = UUID.randomUUID();

        // Receiver headers echoed back by SA-server in the RabbitMQ callback
        Map<String, String> receiverHeaders = Map.of(
            "executionId", context.getProcessExecutionId().toString(),
            "completedStepIndex", String.valueOf(context.getStepOrder()),
            "caseS3Key", context.getCaseS3Key(),
            "resultType", ResultType.SECURITY_ANALYSIS.name()
        );

        // TODO: call security-analysis-server's run endpoint
        // Fire-and-forget: SA-server runs the computation asynchronously
        // and sends a callback to stepResultDestination with receiverHeaders
        securityAnalysisRestClient.run(
            context.getCaseS3Key(),
            context.getConfig().securityAnalysisParametersUuid(),
            context.getConfig().loadflowParametersUuid(),
            resultUuid,
            stepResultDestination,
            receiverHeaders
        );
    }
}
