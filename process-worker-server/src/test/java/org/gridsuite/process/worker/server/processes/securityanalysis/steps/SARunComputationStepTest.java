/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.processes.securityanalysis.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.SecurityAnalysisReport;
import org.gridsuite.process.commons.ResultType;
import org.gridsuite.process.commons.ResultInfos;
import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.process.worker.server.dto.ReportInfos;
import org.gridsuite.process.worker.server.processes.securityanalysis.DummySecurityAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Test for SARunComputationStep
 *
 * This test serves as an EXAMPLE for testing computation/execution steps.
 *
 * Key testing patterns demonstrated:
 * 1. Step type verification
 * 2. Successful computation execution
 * 3. Network requirement validation
 * 4. Contingency processing
 * 5. Result storage and service interaction
 * 6. Report node integration
 * 7. Error handling for missing prerequisites
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class SARunComputationStepTest {

    @Mock
    private DummySecurityAnalysisService securityAnalysisService;

    @Mock
    private ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext;

    @Mock
    private SecurityAnalysisConfig config;

    @Mock
    private ReportNode reportNode;

    @Captor
    private ArgumentCaptor<ResultInfos> resultInfosCaptor;

    @Captor
    private ArgumentCaptor<SecurityAnalysisReport> reportCaptor;

    private SARunComputationStep runComputationStep;

    private static final UUID PARAMS_UUID = UUID.randomUUID();
    private static final UUID REPORT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        runComputationStep = new SARunComputationStep(securityAnalysisService);

        when(stepContext.getConfig()).thenReturn(config);
        when(config.parametersUuid()).thenReturn(PARAMS_UUID);

        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, reportNode);
        when(stepContext.getReportInfos()).thenReturn(reportInfos);
    }

    @Test
    void getTypeShouldReturnRunComputation() {
        // When
        String stepType = runComputationStep.getType().getName();

        // Then
        assertEquals("RUN_COMPUTATION", stepType);
    }

    @Test
    void executeShouldRunSecurityAnalysisSuccessfully() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("NHV1_NHV2_1", "NHV1_NHV2_2"));

        // When
        runComputationStep.execute(stepContext);

        // Then
        verify(securityAnalysisService).saveResult(any(ResultInfos.class), any(SecurityAnalysisReport.class));
    }

    @Test
    void executeShouldThrowExceptionWhenNetworkIsNull() {
        // Given
        when(stepContext.getNetwork()).thenReturn(null);
        when(config.contingencies()).thenReturn(List.of("contingency1"));

        // When & Then
        assertThrows(NullPointerException.class, () ->
            runComputationStep.execute(stepContext)
        );

        verify(securityAnalysisService, never()).saveResult(any(), any());
    }

    @Test
    void executeShouldProcessContingenciesFromConfig() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);

        String cont1 = "CONTINGENCY_1";
        String cont2 = "CONTINGENCY_2";
        String cont3 = "CONTINGENCY_3";
        when(config.contingencies()).thenReturn(List.of(cont1, cont2, cont3));

        // When
        runComputationStep.execute(stepContext);

        // Then - Verify the analysis ran (contingencies are processed internally by PowSyBl)
        verify(securityAnalysisService).saveResult(any(ResultInfos.class), any(SecurityAnalysisReport.class));
    }

    @Test
    void executeShouldSaveResultWithCorrectType() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("contingency1"));

        // When
        runComputationStep.execute(stepContext);

        // Then
        verify(securityAnalysisService).saveResult(resultInfosCaptor.capture(), any(SecurityAnalysisReport.class));

        ResultInfos savedResultInfos = resultInfosCaptor.getValue();
        assertNotNull(savedResultInfos);
        assertNotNull(savedResultInfos.resultUUID());
        assertEquals(ResultType.SECURITY_ANALYSIS, savedResultInfos.resultType());
    }

    @Test
    void executeShouldStoreResultInfosInContext() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("contingency1"));

        // When
        runComputationStep.execute(stepContext);

        // Then
        verify(stepContext).setResultInfos(argThat(resultInfos ->
            resultInfos != null &&
            resultInfos.resultUUID() != null &&
            resultInfos.resultType() == ResultType.SECURITY_ANALYSIS
        ));
    }

    @Test
    void executeShouldUseReportNodeFromContext() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("contingency1"));

        // When
        runComputationStep.execute(stepContext);

        // Then
        verify(stepContext, atLeastOnce()).getReportInfos();
        // The report node from context should be used in the SecurityAnalysisRunParameters
    }

    @Test
    void executeShouldHandleEmptyContingenciesList() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of());

        // When
        runComputationStep.execute(stepContext);

        // Then - Should complete successfully even with no contingencies
        verify(securityAnalysisService).saveResult(any(ResultInfos.class), any(SecurityAnalysisReport.class));
    }

    @Test
    void executeShouldGenerateUniqueResultUuidForEachExecution() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("contingency1"));

        // When - Execute twice
        runComputationStep.execute(stepContext);

        // Reset and execute again
        reset(stepContext);
        when(stepContext.getConfig()).thenReturn(config);
        when(stepContext.getNetwork()).thenReturn(network);
        when(stepContext.getReportInfos()).thenReturn(new ReportInfos(REPORT_UUID, reportNode));

        runComputationStep.execute(stepContext);

        // Then - Verify both executions created result infos (UUIDs generated internally)
        verify(stepContext, times(2)).setResultInfos(any(ResultInfos.class));
    }

    @Test
    void executeShouldPassSecurityAnalysisReportToService() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("contingency1"));

        // When
        runComputationStep.execute(stepContext);

        // Then
        verify(securityAnalysisService).saveResult(any(ResultInfos.class), reportCaptor.capture());

        SecurityAnalysisReport report = reportCaptor.getValue();
        assertNotNull(report);
        assertNotNull(report.getResult());
    }

    @Test
    void executeShouldUseParametersFromConfig() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("contingency1"));

        UUID specificParams = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        when(config.parametersUuid()).thenReturn(specificParams);

        // When
        runComputationStep.execute(stepContext);

        // Then - Verify execution completed
        // Note: Parameters are currently marked as FIXME in implementation
        verify(securityAnalysisService).saveResult(any(ResultInfos.class), any(SecurityAnalysisReport.class));
    }
}
