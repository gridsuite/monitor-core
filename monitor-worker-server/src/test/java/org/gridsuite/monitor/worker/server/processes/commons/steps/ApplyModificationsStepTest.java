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
import org.gridsuite.monitor.commons.ModifyingProcessConfig;
import org.gridsuite.monitor.worker.server.client.NetworkModificationRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.report.ReportInfos;
import org.gridsuite.monitor.worker.server.services.FilterService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationService;
import org.gridsuite.monitor.worker.server.services.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.function.ThrowingConsumer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
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
    private NetworkModificationRestClient networkModificationRestClient;

    @Mock
    private FilterService filterService;

    @Mock
    private S3Service s3Service;

    @Mock
    private ModifyingProcessConfig config;

    private ApplyModificationsStep<ModifyingProcessConfig> applyModificationsStep;

    @Mock
    private ProcessStepExecutionContext<ModifyingProcessConfig> stepContext;

    private static final UUID MODIFICATION_UUID = UUID.randomUUID();
    private static final UUID REPORT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(stepContext.getConfig()).thenReturn(config);

        applyModificationsStep = new ApplyModificationsStep<>(networkModificationService, networkModificationRestClient, s3Service, filterService);
    }

    @Test
    void executeApplyModificationsWhenModificationUuidsNotEmpty() {
        setUpReportInfos();
        when(config.modificationUuids()).thenReturn(List.of(MODIFICATION_UUID));
        String stepType = applyModificationsStep.getType().getName();
        assertEquals("APPLY_MODIFICATIONS", stepType);

        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());

        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(networkModificationRestClient.getModifications(any(List.class))).thenReturn(modificationInfos);
        doNothing().when(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        applyModificationsStep.execute(stepContext);
        verify(networkModificationRestClient).getModifications(any(List.class));
        verify(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));
    }

    @Test
    void executeDoesNothingWhenModificationUuidsEmpty() {
        when(config.modificationUuids()).thenReturn(Collections.emptyList());

        applyModificationsStep.execute(stepContext);

        verifyNoInteractions(networkModificationService);
        verifyNoInteractions(networkModificationRestClient);
        verifyNoInteractions(filterService);
        verifyNoInteractions(s3Service);
    }

    @Test
    void executeApplyModificationsDebugOn() throws IOException {
        setUpReportInfos();
        when(config.modificationUuids()).thenReturn(List.of(MODIFICATION_UUID));
        String stepType = applyModificationsStep.getType().getName();
        assertEquals("APPLY_MODIFICATIONS", stepType);

        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());

        Network network = mock(Network.class);
        when(stepContext.getNetwork()).thenReturn(network);
        when(networkModificationRestClient.getModifications(any(List.class))).thenReturn(modificationInfos);
        doNothing().when(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        // --- mock data specific to debug behaviour ---
        String debugFileLocation = "debug/file/location";
        when(stepContext.getDebugFileLocation()).thenReturn(debugFileLocation);
        when(stepContext.getProcessStepType()).thenReturn(CommonStepType.APPLY_MODIFICATIONS);
        when(stepContext.getStepOrder()).thenReturn(7);

        // -- execute method
        applyModificationsStep.execute(stepContext);

        verify(networkModificationRestClient).getModifications(any(List.class));
        verify(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        // --- verify debug behaviour ---
        ArgumentCaptor<ThrowingConsumer<Path>> networkWriterCapture = ArgumentCaptor.forClass(ThrowingConsumer.class);

        verify(s3Service).exportCompressedToS3(
            eq(debugFileLocation + "/APPLY_MODIFICATIONS_7/debug.xiidm.gz"),
            eq("debug"),
            eq(".xiidm"), // very important - file suffix is very important when using network.write(...)
            networkWriterCapture.capture()
        );

        // --- assert networkWriterCapture.get() is actually calling network.write() ---
        Path mockedPath = mock(Path.class);
        ThrowingConsumer<Path> networkWriter = networkWriterCapture.getValue();

        networkWriter.accept(mockedPath);

        verify(network).write("XIIDM", null, mockedPath);
    }

    private void setUpReportInfos() {
        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build());
        when(stepContext.getReportInfos()).thenReturn(reportInfos);
    }
}
