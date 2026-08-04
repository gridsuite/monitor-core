/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.shortcircuit.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.shortcircuit.*;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.worker.server.clients.ShortCircuitRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit.ShortCircuitParametersInfos;
import org.gridsuite.monitor.worker.server.process.shortcircuit.ShortCircuitStepType;
import org.gridsuite.monitor.worker.server.services.ShortCircuitParametersService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Component
public class ShortCircuitRunComputationStep extends AbstractProcessStep<ShortCircuitConfig> {
    private final ShortCircuitParametersService shortCircuitParametersService;
    private final ShortCircuitRestClient shortCircuitRestClient;

    public ShortCircuitRunComputationStep(ShortCircuitParametersService shortCircuitParametersService, ShortCircuitRestClient shortCircuitRestClient) {
        super(ShortCircuitStepType.RUN_SC_COMPUTATION);
        this.shortCircuitParametersService = shortCircuitParametersService;
        this.shortCircuitRestClient = shortCircuitRestClient;
    }

    @Override
    public void execute(ProcessStepExecutionContext<ShortCircuitConfig> context) {
        Objects.requireNonNull(context.getNetwork());

        ReportNode reportNode = context.getReportNode();

        try {
            ShortCircuitParametersInfos parametersInfos = shortCircuitRestClient.getParameters(context.getConfig().shortCircuitParametersUuid());
            ShortCircuitParameters commonParameters = parametersInfos.getCommonParameters();
            commonParameters.setWithFortescueResult(false);
            // TODO: in shortcircuit-server, retrieved parameters are post processed before being passed to the short-circuit calculation

            String provider = parametersInfos.getProvider() != null ? parametersInfos.getProvider() : "Courcirc";
            Map<String, String> specificParameters = parametersInfos.getSpecificParametersPerProvider().get(provider);

            List<Fault> faults = shortCircuitParametersService.getAllBusFaults(context.getNetwork(), specificParameters);

            shortCircuitParametersService.checkInconsistentVoltageLevels(context.getNetwork());

            ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(
                context.getNetwork(),
                faults,
                commonParameters,
                LocalComputationManager.getDefault(),
                List.of(),
                reportNode);
            // TODO: use the computationManager from gridsuite-computation?

            ResultInfos resultInfos = new ResultInfos(UUID.randomUUID(), ResultType.SHORT_CIRCUIT);
            shortCircuitRestClient.saveResult(resultInfos.resultUUID(), result);
            context.setResultInfos(resultInfos);
        } catch (Exception e) {
            reportNode.newReportNode()
                .withMessageTemplate("monitor.worker.server.shortcircuit.step.error")
                .withUntypedValue("errorMessage", e.getMessage())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
            throw e;
        }
    }
}
