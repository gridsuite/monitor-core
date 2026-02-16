/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.processes.commons.steps;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.gridsuite.modification.dto.AttributeModification;
import org.gridsuite.modification.dto.LoadModificationInfos;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.dto.OperationType;
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessType;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.ReportInfos;
import org.gridsuite.monitor.worker.server.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.function.ThrowingConsumer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ApplyModificationsStepTest {

    @Mock
    private NetworkModificationService networkModificationService;

    @Mock
    private NetworkModificationRestService networkModificationRestService;

    @Mock
    private FilterService filterService;

    @Mock
    private S3Service s3Service;

    @Mock
    private ProcessConfig config;

    private ApplyModificationsStep<ProcessConfig> applyModificationsStep;

    @Mock
    private ProcessStepExecutionContext<ProcessConfig> stepContext;

    private static final UUID MODIFICATION_UUID = UUID.randomUUID();
    private static final UUID REPORT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        applyModificationsStep = new ApplyModificationsStep<>(networkModificationService, networkModificationRestService, s3Service, filterService);
        when(config.modificationUuids()).thenReturn(List.of(MODIFICATION_UUID));
        when(stepContext.getConfig()).thenReturn(config);
        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build());
        when(stepContext.getReportInfos()).thenReturn(reportInfos);
    }

    @Test
    void executeApplyModifications() {
        String stepType = applyModificationsStep.getType().getName();
        assertEquals("APPLY_MODIFICATIONS", stepType);

        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());

        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(networkModificationRestService.getModifications(any(List.class))).thenReturn(modificationInfos);
        doNothing().when(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        applyModificationsStep.execute(stepContext);
        verify(networkModificationRestService).getModifications(any(List.class));
        verify(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));
    }

    @Test
    void executeApplyModificationsDebugOn() throws IOException {
        String stepType = applyModificationsStep.getType().getName();
        assertEquals("APPLY_MODIFICATIONS", stepType);

        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());

        Network network = mock(Network.class);
        when(stepContext.getNetwork()).thenReturn(network);
        when(networkModificationRestService.getModifications(any(List.class))).thenReturn(modificationInfos);
        doNothing().when(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        // --- mock data specific to debug behaviour ---
        when(stepContext.isDebug()).thenReturn(true);
        when(stepContext.getExecutionEnvironment()).thenReturn("execution_env");
        UUID processExecutionId = UUID.randomUUID();
        when(stepContext.getProcessExecutionId()).thenReturn(processExecutionId);
        when(stepContext.getProcessStepType()).thenReturn(CommonStepType.APPLY_MODIFICATIONS);
        when(stepContext.getStepOrder()).thenReturn(7);
        when(config.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);

        // -- execute method
        applyModificationsStep.execute(stepContext);

        verify(networkModificationRestService).getModifications(any(List.class));
        verify(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        // --- verify debug behaviour ---
        ArgumentCaptor<ThrowingConsumer<Path>> networkWriterCapture = ArgumentCaptor.forClass(ThrowingConsumer.class);

        verify(s3Service).exportCompressedToS3(
            eq("execution_env_debug/process/SECURITY_ANALYSIS/" + processExecutionId + "/APPLY_MODIFICATIONS_7/debug.xiidm.gz"),
            eq("debug.xiidm.gz"),
            networkWriterCapture.capture()
        );

        // --- assert network has been written to export method ---
        Path mockedPath = mock(Path.class);
        ThrowingConsumer<Path> networkWriter = networkWriterCapture.getValue();

        networkWriter.accept(mockedPath);

        verify(network).write("XIIDM", null, mockedPath);
    }
}
