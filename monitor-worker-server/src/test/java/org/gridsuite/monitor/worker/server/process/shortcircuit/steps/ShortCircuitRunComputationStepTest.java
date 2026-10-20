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
import org.gridsuite.monitor.worker.server.orchestrator.TaskManager;
import org.gridsuite.monitor.worker.server.process.shortcircuit.ShortCircuitStepType;
import org.gridsuite.monitor.worker.server.services.FileService;
import org.gridsuite.monitor.worker.server.services.S3Service;
import org.gridsuite.monitor.worker.server.services.ShortCircuitParametersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    private S3Service s3Service;

    @Spy
    private FileService fileService;

    private ShortCircuitRunComputationStep runComputationStep;
    private UUID executionId;

    private static final UUID PARAMS_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TaskManager taskManager = new TaskManager();
        executionId = UUID.randomUUID();
        taskManager.registerExecution(executionId);
        runComputationStep = new ShortCircuitRunComputationStep(
                shortCircuitParametersService, shortCircuitRestClient, s3Service, fileService, taskManager, "ShortCircuit-provider");

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
        testExecute(false);
    }

    @Test
    void executeSuccessDebugOn() {
        testExecute(true);
    }

    private void testExecute(boolean debug) {
        testExecute(debug, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"computation", "upload"})
    void executeDebugFailureClosesDirectory(String failingStage) {
        testExecute(true, failingStage);
    }

    private void testExecute(boolean debug, String failingStage) {
        when(stepContext.getProcessExecutionId()).thenReturn(executionId);
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
        when(shortCircuitParametersService.buildParameters(parametersInfos, "ShortCircuit-provider", Collections.emptyMap()))
            .thenReturn(commonParameters);
        when(shortCircuitParametersService.getAllBusFaults(eq(network), any()))
            .thenReturn(List.of(mock(Fault.class)));
        doNothing().when(shortCircuitParametersService).checkInconsistentVoltageLevels(eq(network), any());

        String debugFileLocation = "debug/file/location";
        if (debug) {
            when(stepContext.getDebugFileLocation()).thenReturn(debugFileLocation);
            when(stepContext.getProcessStepType()).thenReturn(ShortCircuitStepType.RUN_SC_COMPUTATION);
            when(stepContext.getStepOrder()).thenReturn(3);
            try {
                doAnswer(invocation -> {
                    Path debugDir = invocation.getArgument(0);
                    assertTrue(Files.isDirectory(debugDir));
                    if ("upload".equals(failingStage)) {
                        throw new IOException("Upload failed");
                    }
                    return null;
                }).when(s3Service).compressAndUploadToS3(
                    any(), eq(debugFileLocation + "/RUN_SC_COMPUTATION_3/debug.zip"), eq("debug.zip"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ShortCircuitAnalysisResult analysisResult = mock(ShortCircuitAnalysisResult.class);
        try (MockedStatic<ShortCircuitAnalysis> shortCircuitAnalysis = mockStatic(ShortCircuitAnalysis.class)) {
            ShortCircuitAnalysis.Runner runner = mock(ShortCircuitAnalysis.Runner.class);
            shortCircuitAnalysis.when(() -> ShortCircuitAnalysis.find("ShortCircuit-provider")).thenReturn(runner);
            if ("computation".equals(failingStage)) {
                when(runner.runAsync(eq(network), any(), any(), any(), any(), any())).thenThrow(new RuntimeException("Computation failed"));
            } else {
                when(runner.runAsync(eq(network), any(), any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(analysisResult));
            }

            if (failingStage == null) {
                runComputationStep.execute(stepContext);
            } else {
                assertThrows(RuntimeException.class, () -> runComputationStep.execute(stepContext));
            }

            verify(runner).runAsync(eq(network), any(), same(commonParameters), any(), any(), any());
            if (debug) {
                Path tempDebugDir = Path.of(commonParameters.getDebugDir());
                assertFalse(Files.exists(tempDebugDir));
                assertTrue(Files.isDirectory(tempDebugDir.getParent()));
                try {
                    verify(s3Service).compressAndUploadToS3(
                        tempDebugDir,
                        debugFileLocation + "/RUN_SC_COMPUTATION_3/debug.zip",
                        "debug.zip"
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        verify(shortCircuitRestClient).getParameters(PARAMS_UUID);
        verify(shortCircuitParametersService).getAllBusFaults(network, parametersInfos.getSpecificParametersPerProvider().get("ShortCircuit-provider"));
        verify(shortCircuitParametersService).checkInconsistentVoltageLevels(eq(network), any());
        if ("computation".equals(failingStage)) {
            verify(shortCircuitRestClient, never()).saveResult(any(), any());
            verify(stepContext, never()).setResultInfos(any());
        } else {
            verify(shortCircuitRestClient).saveResult(any(UUID.class), same(analysisResult));
            verify(stepContext).setResultInfos(argThat(resultInfos ->
                resultInfos.resultUUID() != null &&
                    resultInfos.resultType() == ResultType.SHORT_CIRCUIT
            ));
        }

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
