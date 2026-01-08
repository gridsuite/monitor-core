/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis.steps;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.LineContingency;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisRunParameters;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.DummySecurityAnalysisService;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.SAStepTypes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class SARunComputationStep extends AbstractProcessStep<SecurityAnalysisConfig> {

    private final DummySecurityAnalysisService securityAnalysisService;

    public SARunComputationStep(DummySecurityAnalysisService securityAnalysisService) {
        super(SAStepTypes.RUN_SA_COMPUTATION);
        this.securityAnalysisService = securityAnalysisService;
    }

    @Override
    public void execute(ProcessStepExecutionContext<SecurityAnalysisConfig> context) {
        Objects.requireNonNull(context.getNetwork());
        // FIXME use params from computation server
        UUID params = context.getConfig().parametersUuid();
        List<String> contingencies = context.getConfig().contingencies();
        // FIXME get contingencies
        List<Contingency> contingencyList = contingencies.stream().map(id -> new Contingency(id, new LineContingency(id))).toList();
        SecurityAnalysisRunParameters runParameters = new SecurityAnalysisRunParameters()
                .setReportNode(context.getReportInfos().reportNode());
        SecurityAnalysisReport result = SecurityAnalysis.run(context.getNetwork(), contingencyList, runParameters);

        ResultInfos resultInfos = new ResultInfos(UUID.randomUUID(), ResultType.SECURITY_ANALYSIS);
        securityAnalysisService.saveResult(resultInfos, result);
        context.setResultInfos(resultInfos);
    }
}
