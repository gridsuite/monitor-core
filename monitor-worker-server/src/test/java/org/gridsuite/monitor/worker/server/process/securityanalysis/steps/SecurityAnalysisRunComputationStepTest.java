/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.securityanalysis.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.LineContingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.SecurityAnalysisRunParameters;
import com.powsybl.security.results.PreContingencyResult;
import org.gridsuite.computation.dto.securityanalysis.EnrichedLimitViolationDTO;
import org.gridsuite.computation.dto.securityanalysis.EnrichedSecurityAnalysisResultDTO;
import org.gridsuite.computation.utils.ComputationResultUtils;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.worker.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.parameters.securityanalysis.SecurityAnalysisInputData;
import org.gridsuite.monitor.worker.server.services.SecurityAnalysisParametersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class SecurityAnalysisRunComputationStepTest {

    @Mock
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    @Mock
    private SecurityAnalysisParametersService securityAnalysisParametersService;

    @Mock
    private ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext;

    @Mock
    private SecurityAnalysisConfig config;

    private SecurityAnalysisRunComputationStep runComputationStep;

    private static final UUID PARAMS_UUID = UUID.randomUUID();
    private static final UUID LOADFLOW_PARAMS_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        runComputationStep = new SecurityAnalysisRunComputationStep(securityAnalysisRestClient, securityAnalysisParametersService);

        when(stepContext.getConfig()).thenReturn(config);
        when(config.securityAnalysisParametersUuid()).thenReturn(PARAMS_UUID);
        when(config.loadflowParametersUuid()).thenReturn(LOADFLOW_PARAMS_UUID);

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build();
        when(stepContext.getReportNode()).thenReturn(reportNode);
    }

    @Test
    void executeRunSecurityAnalysis() {
        Network network = mock(Network.class);
        Contingency contingency = new Contingency("NHV1_NHV2_1", "NHV1_NHV2_1", List.of(new LineContingency("NHV1_NHV2_1")));
        SecurityAnalysisInputData inputData = new SecurityAnalysisInputData(new SecurityAnalysisParameters(), List.of(contingency), "OpenLoadFlow");
        when(stepContext.getNetwork()).thenReturn(network);
        when(securityAnalysisParametersService.buildSecurityAnalysisInputData(PARAMS_UUID, LOADFLOW_PARAMS_UUID, network))
            .thenReturn(inputData);

        SecurityAnalysisResult analysisResult = mock(SecurityAnalysisResult.class);
        PreContingencyResult preContingencyResult = mock(PreContingencyResult.class);
        LimitViolationsResult limitViolationsResult = mock(LimitViolationsResult.class);
        com.powsybl.contingency.violations.LimitViolation violation = mock(com.powsybl.contingency.violations.LimitViolation.class);

        when(analysisResult.getPreContingencyResult()).thenReturn(preContingencyResult);
        when(analysisResult.getPostContingencyResults()).thenReturn(Collections.emptyList());
        when(preContingencyResult.getLimitViolationsResult()).thenReturn(limitViolationsResult);
        when(limitViolationsResult.getLimitViolations()).thenReturn(List.of(violation));

        SecurityAnalysisReport analysisReport = mock(SecurityAnalysisReport.class);
        when(analysisReport.getResult()).thenReturn(analysisResult);
        try (MockedStatic<SecurityAnalysis> securityAnalysis = mockStatic(SecurityAnalysis.class);
             MockedStatic<ComputationResultUtils> computationResultUtils = mockStatic(ComputationResultUtils.class)) {
            SecurityAnalysis.Runner runner = mock(SecurityAnalysis.Runner.class);
            securityAnalysis.when(() -> SecurityAnalysis.find("OpenLoadFlow")).thenReturn(runner);
            when(runner.run(any(), any(), any(SecurityAnalysisRunParameters.class))).thenReturn(analysisReport);

            computationResultUtils.when(() -> ComputationResultUtils.getPatlLimit(any(), any())).thenReturn(100.0);
            computationResultUtils.when(() -> ComputationResultUtils.computeLoading(any(), any())).thenReturn(50.0);
            computationResultUtils.when(() -> ComputationResultUtils.getNextLimitName(any(), any())).thenReturn("TATL");
            computationResultUtils.when(() -> ComputationResultUtils.getViolationLocationId(any(), any())).thenReturn("Location1");
            computationResultUtils.when(() -> ComputationResultUtils.calculateActualOverloadDuration(any(), any())).thenReturn(3600);
            computationResultUtils.when(() -> ComputationResultUtils.getViolationId(any())).thenReturn("violationId");

            runComputationStep.execute(stepContext);

            verify(runner).run(eq(network), eq(List.of(contingency)), any(SecurityAnalysisRunParameters.class));
        }

        String stepType = runComputationStep.getType().getName();
        assertEquals("RUN_SA_COMPUTATION", stepType);

        verify(securityAnalysisParametersService).buildSecurityAnalysisInputData(PARAMS_UUID, LOADFLOW_PARAMS_UUID, network);
        verify(securityAnalysisRestClient).saveResult(any(UUID.class), argThat(enrichedResult -> {
            assertEquals(analysisResult, enrichedResult.getResult());
            Map<String, EnrichedLimitViolationDTO> preEnrichment = enrichedResult.getPreContingencyEnrichment();
            assertNotNull(preEnrichment);
            String violationId = "violationId";
            assertTrue(preEnrichment.containsKey(violationId));
            EnrichedLimitViolationDTO enrichedViolation = preEnrichment.get(violationId);
            assertEquals(100.0, enrichedViolation.getPatlLimit(), 0.001);
            assertEquals(50.0, enrichedViolation.getPatlLoading(), 0.001);
            assertEquals("TATL", enrichedViolation.getNextLimitName());
            assertEquals("Location1", enrichedViolation.getLocationId());
            assertEquals(3600, enrichedViolation.getAcceptableDuration());
            return true;
        }));
        verify(stepContext).setResultInfos(argThat(resultInfos ->
                        resultInfos.resultUUID() != null &&
                        resultInfos.resultType() == ResultType.SECURITY_ANALYSIS
        ));
    }

    @Test
    void executeRunSecurityAnalysisFailed() {
        Network network = mock(Network.class);
        when(stepContext.getNetwork()).thenReturn(network);
        when(securityAnalysisParametersService.buildSecurityAnalysisInputData(
            PARAMS_UUID, LOADFLOW_PARAMS_UUID, network)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
            () -> runComputationStep.execute(stepContext));

        verify(securityAnalysisRestClient, never()).saveResult(any(UUID.class), any(EnrichedSecurityAnalysisResultDTO.class));
        verify(stepContext, never()).setResultInfos(any());
    }
}
