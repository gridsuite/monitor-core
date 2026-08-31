/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.shortcircuit.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.shortcircuit.Fault;
import com.powsybl.shortcircuit.ShortCircuitAnalysis;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.worker.server.clients.ShortCircuitRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.parameters.shortcircuit.ShortCircuitParametersInfos;
import org.gridsuite.monitor.worker.server.services.ShortCircuitParametersService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
class ShortCircuitRunComputationStepTest {

    @Mock
    private ShortCircuitParametersService shortCircuitParametersService;

    @Mock
    private ShortCircuitRestClient shortCircuitRestClient;

    @Mock
    private ProcessStepExecutionContext<ShortCircuitConfig> stepContext;

    @Mock
    private ShortCircuitConfig config;

    private ShortCircuitRunComputationStep runComputationStep;

    private static final UUID PARAMS_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        runComputationStep = new ShortCircuitRunComputationStep(shortCircuitParametersService, shortCircuitRestClient, "ShortCircuit-provider");

        when(stepContext.getConfig()).thenReturn(config);
        when(config.shortCircuitParametersUuid()).thenReturn(PARAMS_UUID);

        ReportNode reportNode = ReportNode.newRootReportNode()
            .withResourceBundles("i18n.reports")
            .withMessageTemplate("test")
            .build();
        when(stepContext.getReportNode()).thenReturn(reportNode);
    }

    @Test
    void executeSuccess() {
        Network network = EurostagTutorialExample1Factory.create();
        ShortCircuitParameters commonParameters = new ShortCircuitParameters();
        ShortCircuitParametersInfos parametersInfos = ShortCircuitParametersInfos.builder()
            .commonParameters(commonParameters)
            .provider("ShortCircuit-provider")
            .specificParametersPerProvider(Map.of("ShortCircuit-provider", Collections.emptyMap()))
            .build();
        when(stepContext.getNetwork())
            .thenReturn(network);
        when(shortCircuitRestClient.getParameters(PARAMS_UUID))
            .thenReturn(parametersInfos);
        when(shortCircuitParametersService.getAllBusFaults(eq(network), any()))
            .thenReturn(List.of(mock(Fault.class)));
        doNothing().when(shortCircuitParametersService).checkInconsistentVoltageLevels(eq(network), any());

        ShortCircuitAnalysisResult analysisResult = mock(ShortCircuitAnalysisResult.class);
        try (MockedStatic<ShortCircuitAnalysis> shortCircuitAnalysis = mockStatic(ShortCircuitAnalysis.class)) {
            shortCircuitAnalysis.when(() -> ShortCircuitAnalysis.run(eq(network), any(), any(), any(), any(), any()))
                .thenReturn(analysisResult);

            runComputationStep.execute(stepContext);
        }

        verify(shortCircuitRestClient).getParameters(PARAMS_UUID);
        verify(shortCircuitParametersService).getAllBusFaults(network, parametersInfos.getSpecificParametersPerProvider().get("ShortCircuit-provider"));
        verify(shortCircuitParametersService).checkInconsistentVoltageLevels(eq(network), any());
        verify(shortCircuitRestClient).saveResult(any(UUID.class), same(analysisResult));
        verify(stepContext).setResultInfos(argThat(resultInfos ->
            resultInfos.resultUUID() != null &&
                resultInfos.resultType() == ResultType.SHORT_CIRCUIT
        ));

        assertEquals("RUN_SC_COMPUTATION", runComputationStep.getType().getName());
    }

    @Test
    void executeFailed() {
        Network network = EurostagTutorialExample1Factory.create();

        when(stepContext.getNetwork()).thenReturn(network);
        when(shortCircuitRestClient.getParameters(any(UUID.class))).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
            () -> runComputationStep.execute(stepContext));

        verify(shortCircuitRestClient, never()).saveResult(any(), any());
        verify(stepContext, never()).setResultInfos(any());
    }
}
