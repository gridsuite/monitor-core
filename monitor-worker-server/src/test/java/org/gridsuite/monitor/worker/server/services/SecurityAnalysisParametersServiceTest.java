/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.contingency.list.IdentifierContingencyList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.identifiers.IdBasedNetworkElementIdentifier;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.LoadFlowParameters;
import org.gridsuite.actions.dto.contingency.AbstractContingencyList;
import org.gridsuite.actions.dto.contingency.IdBasedContingencyList;
import org.gridsuite.monitor.worker.server.clients.ActionsRestClient;
import org.gridsuite.monitor.worker.server.clients.FilterRestClient;
import org.gridsuite.monitor.worker.server.clients.LoadFlowRestClient;
import org.gridsuite.monitor.worker.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.ContingencyListsInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.IdNameInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisInputData;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisParametersValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class SecurityAnalysisParametersServiceTest {
    @Mock
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    @Mock
    private LoadFlowRestClient loadFlowRestClient;

    @Mock
    private ActionsRestClient actionsRestClient;

    @Mock
    private FilterRestClient filterRestClient;

    private SecurityAnalysisParametersService securityAnalysisParametersService;

    @BeforeEach
    void setUp() {
        securityAnalysisParametersService = new SecurityAnalysisParametersService(securityAnalysisRestClient, loadFlowRestClient, actionsRestClient, filterRestClient);
    }

    @Test
    void buildSecurityAnalysisInputData() {
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        UUID loadflowParametersUuid = UUID.randomUUID();
        Network network = EurostagTutorialExample1Factory.create();

        UUID contingencyListId = UUID.randomUUID();
        List<ContingencyListsInfos> contingencyListsInfos = List.of(new ContingencyListsInfos(
                List.of(new IdNameInfos(contingencyListId, "contingencyList")),
                "activated contingency lists",
                true));
        SecurityAnalysisParametersValues securityAnalysisParametersValues = SecurityAnalysisParametersValues.builder()
                .lowVoltageAbsoluteThreshold(10)
                .lowVoltageProportionalThreshold(11)
                .highVoltageAbsoluteThreshold(12)
                .highVoltageProportionalThreshold(13)
                .flowProportionalThreshold(14)
                .contingencyListsInfos(contingencyListsInfos)
                .build();
        LoadFlowParametersInfos loadFlowParametersInfos = LoadFlowParametersInfos.builder()
            .commonParameters(LoadFlowParameters.load())
            .specificParametersPerProvider(Map.of())
            .build();
        List<UUID> contingencyListUuids = List.of(contingencyListId);
        IdBasedContingencyList idBasedContingencyList = new IdBasedContingencyList(contingencyListId, Instant.now(),
            new IdentifierContingencyList("c1", List.of(new IdBasedNetworkElementIdentifier("GEN", "c1"))));
        List<AbstractContingencyList> persistentContingencyList = List.of(idBasedContingencyList);

        when(securityAnalysisRestClient.getParameters(securityAnalysisParametersUuid)).thenReturn(securityAnalysisParametersValues);
        when(loadFlowRestClient.getParameters(loadflowParametersUuid)).thenReturn(loadFlowParametersInfos);
        when(actionsRestClient.getPersistentContingencyLists(contingencyListUuids)).thenReturn(persistentContingencyList);

        SecurityAnalysisInputData inputData = securityAnalysisParametersService.buildSecurityAnalysisInputData(securityAnalysisParametersUuid, loadflowParametersUuid, network);

        assertThat(inputData.securityAnalysisParameters().getLoadFlowParameters()).usingRecursiveComparison().isEqualTo(loadFlowParametersInfos.getCommonParameters());
        assertThat(inputData.contingencies()).hasSize(1);
        assertThat(inputData.contingencies().get(0).getId()).isEqualTo("c1");
        assertThat(inputData.contingencies().get(0).getElements()).hasSize(1);
        assertThat(inputData.contingencies().get(0).getElements().get(0).getId()).isEqualTo("GEN");

        verify(securityAnalysisRestClient, times(1)).getParameters(securityAnalysisParametersUuid);
        verify(loadFlowRestClient, times(1)).getParameters(loadflowParametersUuid);
        verify(actionsRestClient, times(1)).getPersistentContingencyLists(contingencyListUuids);
    }
}
