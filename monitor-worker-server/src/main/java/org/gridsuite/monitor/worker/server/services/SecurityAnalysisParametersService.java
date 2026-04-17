/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisParameters;
import org.gridsuite.actions.ContingencyListEvaluator;
import org.gridsuite.actions.dto.contingency.AbstractContingencyList;
import org.gridsuite.actions.dto.evaluation.ContingencyInfos;
import org.gridsuite.monitor.worker.server.clients.ActionsRestClient;
import org.gridsuite.monitor.worker.server.clients.FilterRestClient;
import org.gridsuite.monitor.worker.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.worker.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadflowParametersInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.ContingencyListsInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.IdNameInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisInputData;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisParametersValues;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Franck Lecyer <franck.lecuyer at rte-france.com>
 */
@Service
public class SecurityAnalysisParametersService {
    private final SecurityAnalysisRestClient securityAnalysisRestClient;
    private final LoadflowRestClient loadflowRestClient;
    private final ActionsRestClient actionsRestClient;
    private final FilterRestClient filterRestClient;

    public SecurityAnalysisParametersService(SecurityAnalysisRestClient securityAnalysisRestClient,
                                             LoadflowRestClient loadflowRestClient,
                                             ActionsRestClient actionsRestClient,
                                             FilterRestClient filterRestClient) {
        this.securityAnalysisRestClient = securityAnalysisRestClient;
        this.loadflowRestClient = loadflowRestClient;
        this.actionsRestClient = actionsRestClient;
        this.filterRestClient = filterRestClient;
    }

    public SecurityAnalysisInputData buildSecurityAnalysisInputData(UUID securityAnalysisParametersUuid, UUID loadflowParametersUuid, Network network) {
        SecurityAnalysisParametersValues securityAnalysisParametersValues = securityAnalysisRestClient.getParameters(securityAnalysisParametersUuid);
        LoadflowParametersInfos loadflowParametersInfos = loadflowRestClient.getParameters(loadflowParametersUuid);

        SecurityAnalysisParameters securityAnalysisParameters = buildSecurityAnalysisParameters(loadflowParametersInfos, securityAnalysisParametersValues);

        List<ContingencyListsInfos> contingencyListInfos = securityAnalysisParametersValues.getContingencyListsInfos();
        List<UUID> contingenciesListUuids = contingencyListInfos != null
            ? contingencyListInfos.stream().flatMap(contingencyListsInfos -> contingencyListsInfos.getContingencyLists().stream().map(IdNameInfos::getId)).toList()
            : List.of();

        List<AbstractContingencyList> persistentContingencyLists = actionsRestClient.getPersistentContingencyLists(contingenciesListUuids);

        List<Contingency> contingencyList = new ArrayList<>();
        ContingencyListEvaluator contingencyListEvaluator = new ContingencyListEvaluator(filterRestClient);

        persistentContingencyLists.forEach(persistentContingencyList -> {
            List<Contingency> contingencies = contingencyListEvaluator.evaluateContingencyList(persistentContingencyList, network)
                .stream()
                .map(ContingencyInfos::getContingency)
                .filter(Objects::nonNull)
                .toList();
            contingencyList.addAll(contingencies);
        });

        return new SecurityAnalysisInputData(securityAnalysisParameters, contingencyList);
    }

    private static SecurityAnalysisParameters buildSecurityAnalysisParameters(LoadflowParametersInfos loadflowParametersInfos,
                                                                              SecurityAnalysisParametersValues securityAnalysisParametersValues) {
        SecurityAnalysisParameters securityAnalysisParameters = new SecurityAnalysisParameters();
        securityAnalysisParameters.setLoadFlowParameters(loadflowParametersInfos.getCommonParameters());
        SecurityAnalysisParameters.IncreasedViolationsParameters increasedViolationsParameters =
            new SecurityAnalysisParameters.IncreasedViolationsParameters(
                securityAnalysisParametersValues.getLowVoltageAbsoluteThreshold(),
                securityAnalysisParametersValues.getLowVoltageProportionalThreshold(),
                securityAnalysisParametersValues.getHighVoltageAbsoluteThreshold(),
                securityAnalysisParametersValues.getHighVoltageProportionalThreshold(),
                securityAnalysisParametersValues.getFlowProportionalThreshold());
        securityAnalysisParameters.setIncreasedViolationsParameters(increasedViolationsParameters);
        return securityAnalysisParameters;
    }
}
