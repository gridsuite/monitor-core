/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.securityanalysis.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.SecurityAnalysisResult;
import org.gridsuite.monitor.commons.ResultType;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.ReportInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.ContingencyListsInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.IdNameInfos;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisParametersValues;
import org.gridsuite.monitor.worker.server.services.ActionsRestService;
import org.gridsuite.monitor.worker.server.services.LoadFlowRestService;
import org.gridsuite.monitor.worker.server.services.SecurityAnalysisRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class SecurityAnalysisRunComputationStepTest {

    @Mock
    private SecurityAnalysisRestService securityAnalysisRestService;

    @Mock
    private LoadFlowRestService loadFlowRestService;

    @Mock
    private ActionsRestService actionsRestService;

    @Mock
    private ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext;

    @Mock
    private SecurityAnalysisConfig config;

    private SecurityAnalysisRunComputationStep runComputationStep;

    private static final UUID PARAMS_UUID = UUID.randomUUID();
    private static final UUID LOADFLOW_PARAMS_UUID = UUID.randomUUID();
    private static final UUID REPORT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        runComputationStep = new SecurityAnalysisRunComputationStep(securityAnalysisRestService, loadFlowRestService, actionsRestService);

        when(stepContext.getConfig()).thenReturn(config);
        when(config.parametersUuid()).thenReturn(PARAMS_UUID);
        when(config.loadflowParametersUuid()).thenReturn(LOADFLOW_PARAMS_UUID);

        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build());
        when(stepContext.getReportInfos()).thenReturn(reportInfos);
    }

    @Test
    void executeRunSecurityAnalysis() {
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(loadFlowRestService.getParameters(LOADFLOW_PARAMS_UUID)).thenReturn(new LoadFlowParametersInfos(null, null, new LoadFlowParameters(), null, null, null));
        when(securityAnalysisRestService.getParameters(PARAMS_UUID)).thenReturn(new SecurityAnalysisParametersValues("OpenLoadFlow", 1, 0.5, 2, 0.7, 1,
            List.of(new ContingencyListsInfos(List.of(new IdNameInfos(UUID.randomUUID(), "name")), "desc", true)), null));

        runComputationStep.execute(stepContext);

        String stepType = runComputationStep.getType().getName();
        assertEquals("RUN_SA_COMPUTATION", stepType);

        verify(loadFlowRestService).getParameters(LOADFLOW_PARAMS_UUID);
        verify(securityAnalysisRestService).getParameters(PARAMS_UUID);
        verify(securityAnalysisRestService).saveResult(
                any(UUID.class),
                any(SecurityAnalysisResult.class)
        );
        verify(stepContext).setResultInfos(argThat(resultInfos ->
                        resultInfos.resultUUID() != null &&
                        resultInfos.resultType() == ResultType.SECURITY_ANALYSIS
        ));
    }
}
