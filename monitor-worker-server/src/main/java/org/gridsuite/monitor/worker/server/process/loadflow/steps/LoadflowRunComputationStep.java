/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.loadflow.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowRunParameters;
import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.worker.server.clients.LoadFlowRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.core.process.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.process.loadflow.LoadflowStepType;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Component
public class LoadflowRunComputationStep extends AbstractProcessStep<LoadFlowConfig> {

    private final LoadFlowRestClient loadflowRestClient;

    protected LoadflowRunComputationStep(LoadFlowRestClient loadflowRestClient) {
        super(LoadflowStepType.RUN_LF_COMPUTATION);
        this.loadflowRestClient = loadflowRestClient;
    }

    @Override
    public void execute(ProcessStepExecutionContext<LoadFlowConfig> context) {
        Objects.requireNonNull(context.getNetwork());

        ReportNode reportNode = context.getReportNode();

        try {
            LoadFlowParameters loadflowParameters = loadflowRestClient.getParameters(context.getConfig().loadflowParametersUuid()).getCommonParameters();
            LoadFlowRunParameters runParameters = new LoadFlowRunParameters()
                .setParameters(loadflowParameters)
                .setReportNode(reportNode);
            LoadFlowResult result = LoadFlow.run(context.getNetwork(), runParameters);

            UUID resultUuid = loadflowRestClient.saveResult(result);
            ResultInfos resultInfos = new ResultInfos(resultUuid, ResultType.LOADFLOW);
            context.setResultInfos(resultInfos);
        } catch (Exception e) {
            reportNode.newReportNode()
                .withMessageTemplate("monitor.worker.server.loadflow.step.error")
                .withUntypedValue("errorMessage", e.getMessage())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
            throw e;
        }
    }
}
