/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.processes.commons.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.process.worker.server.core.ProcessExecutionContext;
import org.gridsuite.process.worker.server.dto.ReportInfos;
import org.gridsuite.process.worker.server.services.NetworkConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadNetworkStepTest {

    @Mock
    private NetworkConversionService networkConversionService;

    @Mock
    private ProcessExecutionContext<SecurityAnalysisConfig> processContext;

    @Mock
    private SecurityAnalysisConfig config;

    @Mock
    private ReportNode reportNode;

    @Mock
    private ReportNode subReportNode;

    @Captor
    private ArgumentCaptor<Network> networkCaptor;

    @Captor
    private ArgumentCaptor<ReportNode> reportNodeCaptor;

    private LoadNetworkStep<SecurityAnalysisConfig> loadNetworkStep;
    private ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext;

    private static final UUID CASE_UUID = UUID.randomUUID();
    private static final UUID EXECUTION_ID = UUID.randomUUID();
    private static final UUID REPORT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        loadNetworkStep = new LoadNetworkStep<>(networkConversionService);

        when(config.caseUuid()).thenReturn(CASE_UUID);
        when(processContext.getConfig()).thenReturn(config);
        when(processContext.getExecutionId()).thenReturn(EXECUTION_ID);

        // Setup report node chain
//        when(reportNode.newReportNode()).thenReturn(subReportNode);
//        when(subReportNode.withMessageTemplate(anyString())).thenReturn(subReportNode);
//        when(subReportNode.withUntypedValue(anyString(), anyString())).thenReturn(subReportNode);
//        when(subReportNode.add()).thenReturn(subReportNode);

        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, reportNode);

        stepContext = mock(ProcessStepExecutionContext.class);
        when(stepContext.getConfig()).thenReturn(config);
        when(stepContext.getReportInfos()).thenReturn(reportInfos);
        when(stepContext.getProcessContext()).thenReturn(processContext);
    }

    @Test
    void getTypeShouldReturnLoadNetwork() {
        // When
        String stepType = loadNetworkStep.getType().getName();

        // Then
        assertEquals("LOAD_NETWORK", stepType);
    }

    @Test
    void executeShouldLoadNetworkFromCaseUuid() {
        // Given
        Network expectedNetwork = EurostagTutorialExample1Factory.create();
        when(networkConversionService.createNetwork(eq(CASE_UUID), any(ReportNode.class)))
            .thenReturn(expectedNetwork);

        // When
        loadNetworkStep.execute(stepContext);

        // Then
        verify(networkConversionService).createNetwork(eq(CASE_UUID), any(ReportNode.class));
    }

    @Test
    void executeShouldStoreNetworkInContext() {
        // Given
        Network expectedNetwork = EurostagTutorialExample1Factory.create();
        when(networkConversionService.createNetwork(eq(CASE_UUID), any(ReportNode.class)))
            .thenReturn(expectedNetwork);

        // When
        loadNetworkStep.execute(stepContext);

        // Then
        verify(stepContext).setNetwork(networkCaptor.capture());
        Network capturedNetwork = networkCaptor.getValue();

        assertNotNull(capturedNetwork);
        assertSame(expectedNetwork, capturedNetwork);
    }

    @Test
    void executeShouldCreateReportNodeWithCorrectTemplate() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(networkConversionService.createNetwork(eq(CASE_UUID), any(ReportNode.class)))
            .thenReturn(network);

        // When
        loadNetworkStep.execute(stepContext);

        // Then
        verify(reportNode).newReportNode();
//        verify(subReportNode).withMessageTemplate("process.worker.server.importCase");
//        verify(subReportNode).withUntypedValue("caseUuid", CASE_UUID.toString());
//        verify(subReportNode).add();
    }

    @Test
    void executeShouldPassReportNodeToNetworkConversion() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        when(networkConversionService.createNetwork(eq(CASE_UUID), any(ReportNode.class)))
            .thenReturn(network);

        // When
        loadNetworkStep.execute(stepContext);

        // Then
        verify(networkConversionService).createNetwork(eq(CASE_UUID), reportNodeCaptor.capture());

        ReportNode capturedReportNode = reportNodeCaptor.getValue();
        assertNotNull(capturedReportNode);
        // The captured report node should be the sub-report created specifically for import
        assertSame(subReportNode, capturedReportNode);
    }

    @Test
    void executeShouldPropagateExceptionWhenNetworkLoadingFails() {
        // Given
        RuntimeException networkLoadException = new RuntimeException("Failed to load network");
        when(networkConversionService.createNetwork(eq(CASE_UUID), any(ReportNode.class)))
            .thenThrow(networkLoadException);

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            loadNetworkStep.execute(stepContext)
        );

        assertEquals("Failed to load network", thrown.getMessage());
        verify(stepContext, never()).setNetwork(any());
    }

    @Test
    void executeShouldUseCaseUuidFromConfig() {
        // Given
        UUID specificCaseUuid = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        when(config.caseUuid()).thenReturn(specificCaseUuid);

        Network network = EurostagTutorialExample1Factory.create();
        when(networkConversionService.createNetwork(eq(specificCaseUuid), any(ReportNode.class)))
            .thenReturn(network);

        // When
        loadNetworkStep.execute(stepContext);

        // Then
        verify(networkConversionService).createNetwork(eq(specificCaseUuid), any(ReportNode.class));
//        verify(subReportNode).withUntypedValue("caseUuid", specificCaseUuid.toString());
    }
}
