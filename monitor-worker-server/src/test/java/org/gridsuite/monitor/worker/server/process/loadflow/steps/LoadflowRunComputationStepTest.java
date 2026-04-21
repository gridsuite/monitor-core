/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.loadflow.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.worker.server.clients.LoadFlowRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.parameters.loadflow.LoadFlowParametersInfos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
class LoadflowRunComputationStepTest {
    @Mock
    private LoadFlowRestClient loadFlowRestClient;

    @Mock
    private ProcessStepExecutionContext<LoadFlowConfig> stepContext;

    @Mock
    private LoadFlowConfig config;

    private LoadflowRunComputationStep runComputationStep;

    private static final UUID PARAMS_UUID = UUID.randomUUID();
    private static final UUID RESULT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        runComputationStep = new LoadflowRunComputationStep(loadFlowRestClient);

        when(stepContext.getConfig()).thenReturn(config);
        when(config.loadflowParametersUuid()).thenReturn(PARAMS_UUID);

        ReportNode reportNode = ReportNode.newRootReportNode()
            .withResourceBundles("i18n.reports")
            .withMessageTemplate("test")
            .build();
        when(stepContext.getReportNode()).thenReturn(reportNode);
    }

    @Test
    void executeRunLoadflow() {
        Network network = EurostagTutorialExample1Factory.create();
        LoadFlowParametersInfos loadflowParametersInfos = LoadFlowParametersInfos.builder()
            .commonParameters(new LoadFlowParameters())
            .build();

        when(stepContext.getNetwork()).thenReturn(network);
        when(loadFlowRestClient.getParameters(PARAMS_UUID)).thenReturn(loadflowParametersInfos);
        when(loadFlowRestClient.saveResult(any(LoadFlowResult.class))).thenReturn(RESULT_UUID);

        runComputationStep.execute(stepContext);

        String stepType = runComputationStep.getType().getName();
        assertEquals("RUN_LF_COMPUTATION", stepType);

        verify(loadFlowRestClient).getParameters(PARAMS_UUID);
        verify(loadFlowRestClient).saveResult(any(LoadFlowResult.class));
        verify(stepContext).setResultInfos(argThat(resultInfos ->
            resultInfos.resultUUID() != null &&
                resultInfos.resultType() == ResultType.LOADFLOW
        ));
    }

    @Test
    void executeRunLoadflowFailed() {
        Network network = EurostagTutorialExample1Factory.create();

        when(stepContext.getNetwork()).thenReturn(network);
        when(loadFlowRestClient.getParameters(any(UUID.class))).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
            () -> runComputationStep.execute(stepContext));

        verify(loadFlowRestClient, never()).saveResult(any());
        verify(stepContext, never()).setResultInfos(any());
    }
}
