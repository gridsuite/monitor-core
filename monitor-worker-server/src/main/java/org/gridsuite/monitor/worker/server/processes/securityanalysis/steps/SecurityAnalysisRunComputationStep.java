/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis.steps;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisRunParameters;
import org.gridsuite.actions.dto.contingency.PersistentContingencyList;
import org.gridsuite.monitor.commons.ResultInfos;
import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.AbstractProcessStep;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.ContingencyListsInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.IdNameInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisParametersValues;
import org.gridsuite.monitor.worker.server.processes.securityanalysis.SecurityAnalysisStepType;
import org.gridsuite.monitor.worker.server.services.ActionsRestService;
import org.gridsuite.monitor.worker.server.services.LoadFlowRestService;
import org.gridsuite.monitor.worker.server.services.SecurityAnalysisRestService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class SecurityAnalysisRunComputationStep extends AbstractProcessStep<SecurityAnalysisConfig> {

    private final SecurityAnalysisRestService securityAnalysisRestService;
    private final LoadFlowRestService loadFlowRestService;
    private final ActionsRestService actionsRestService;

    public SecurityAnalysisRunComputationStep(SecurityAnalysisRestService securityAnalysisRestService,
                                              LoadFlowRestService loadFlowRestService,
                                              ActionsRestService actionsRestService) {
        super(SecurityAnalysisStepType.RUN_SA_COMPUTATION);
        this.securityAnalysisRestService = securityAnalysisRestService;
        this.loadFlowRestService = loadFlowRestService;
        this.actionsRestService = actionsRestService;
    }

    @Override
    public void execute(ProcessStepExecutionContext<SecurityAnalysisConfig> context) {
        Objects.requireNonNull(context.getNetwork());

        SecurityAnalysisParametersValues securityAnalysisParametersValues = securityAnalysisRestService.getParameters(context.getConfig().parametersUuid());
        LoadFlowParametersInfos loadFlowParametersInfos = loadFlowRestService.getParameters(context.getConfig().loadflowParametersUuid());

        SecurityAnalysisParameters securityAnalysisParameters = new SecurityAnalysisParameters();
        securityAnalysisParameters.setLoadFlowParameters(loadFlowParametersInfos.getCommonParameters());
        SecurityAnalysisParameters.IncreasedViolationsParameters increasedViolationsParameters =
            new SecurityAnalysisParameters.IncreasedViolationsParameters(
                securityAnalysisParametersValues.getLowVoltageAbsoluteThreshold(),
                securityAnalysisParametersValues.getLowVoltageProportionalThreshold(),
                securityAnalysisParametersValues.getHighVoltageAbsoluteThreshold(),
                securityAnalysisParametersValues.getHighVoltageProportionalThreshold(),
                securityAnalysisParametersValues.getFlowProportionalThreshold());
        securityAnalysisParameters.setIncreasedViolationsParameters(increasedViolationsParameters);

        List<ContingencyListsInfos> contingencyListInfos = securityAnalysisParametersValues.getContingencyListsInfos();
        List<UUID> contingenciesListUuids = contingencyListInfos.stream().flatMap(contingencyListsInfos -> contingencyListsInfos.getContingencyLists().stream().map(IdNameInfos::getId)).toList();
        List<PersistentContingencyList> persistentContingencyLists = actionsRestService.getPersistentContingencyLists(contingenciesListUuids);
        List<Contingency> contingencyList = new ArrayList<>();
        persistentContingencyLists.forEach(persistentContingencyList -> {
            ContingencyList contingencies = persistentContingencyList.toPowsyblContingencyList(context.getNetwork());
            contingencyList.addAll(contingencies.getContingencies(context.getNetwork()));
        });

        SecurityAnalysisRunParameters runParameters = new SecurityAnalysisRunParameters()
            .setSecurityAnalysisParameters(securityAnalysisParameters)
            .setReportNode(context.getReportInfos().reportNode());
        SecurityAnalysisReport saReport = SecurityAnalysis.run(context.getNetwork(), contingencyList, runParameters);

        ResultInfos resultInfos = new ResultInfos(UUID.randomUUID(), ResultType.SECURITY_ANALYSIS);
        securityAnalysisRestService.saveResult(resultInfos.resultUUID(), saReport.getResult());
        context.setResultInfos(resultInfos);
    }
}
