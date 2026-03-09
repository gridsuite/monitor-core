/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisRunParameters;
import org.apache.commons.lang3.tuple.Pair;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.SecurityAnalysisStepType;
import org.gridsuite.monitor.worker.server.report.MonitorWorkerServerReportResourceBundle;
import org.gridsuite.monitor.worker.server.services.SecurityAnalysisParametersService;
import org.gridsuite.monitor.worker.server.services.SecurityAnalysisRestService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class SecurityAnalysisRunComputationStep extends AbstractProcessStep<SecurityAnalysisConfig> {

    private final SecurityAnalysisRestService securityAnalysisRestService;
    private final SecurityAnalysisParametersService securityAnalysisParametersService;

    public SecurityAnalysisRunComputationStep(SecurityAnalysisRestService securityAnalysisRestService,
                                              SecurityAnalysisParametersService securityAnalysisParametersService) {
        super(SecurityAnalysisStepType.RUN_SA_COMPUTATION);
        this.securityAnalysisRestService = securityAnalysisRestService;
        this.securityAnalysisParametersService = securityAnalysisParametersService;
    }

    @Override
    public void execute(ProcessStepExecutionContext<SecurityAnalysisConfig> context) {
        Objects.requireNonNull(context.getNetwork());

        ReportNode reportNode = context.getReportInfos().reportNode();

        try {
            Pair<SecurityAnalysisParameters, List<Contingency>> inputData = securityAnalysisParametersService.buildSecurityAnalysisInputData(
                context.getConfig().parametersUuid(), context.getConfig().loadflowParametersUuid(), context.getUserId(), context.getNetwork());

            SecurityAnalysisRunParameters runParameters = new SecurityAnalysisRunParameters()
                .setSecurityAnalysisParameters(inputData.getLeft())
                .setReportNode(reportNode);
            SecurityAnalysisReport saReport = SecurityAnalysis.run(context.getNetwork(), inputData.getRight(), runParameters);

            ResultInfos resultInfos = new ResultInfos(UUID.randomUUID(), ResultType.SECURITY_ANALYSIS);
            securityAnalysisRestService.saveResult(resultInfos.resultUUID(), saReport.getResult());
            context.setResultInfos(resultInfos);
        } catch (Exception e) {
            reportNode.newReportNode()
                .withResourceBundles(MonitorWorkerServerReportResourceBundle.BASE_NAME)
                .withMessageTemplate("monitor.worker.server.securityanalysis.step.error")
                .withUntypedValue("errorMessage", e.getMessage())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
            throw e;
        }
    }
}
