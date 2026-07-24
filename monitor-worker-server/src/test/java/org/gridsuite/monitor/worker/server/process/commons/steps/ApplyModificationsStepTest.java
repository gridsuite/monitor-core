/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.process.commons.steps;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.gridsuite.modification.dto.AttributeModification;
import org.gridsuite.modification.dto.LoadModificationInfos;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.dto.OperationType;
import org.gridsuite.monitor.commons.types.processconfig.ModificationInfo;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.worker.server.clients.NetworkModificationRestClient;
import org.gridsuite.monitor.worker.server.core.context.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.networkmodifications.NetworkModificationsWithMissingInfo;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private ProcessConfig config;

    private ApplyModificationsStep<ProcessConfig> applyModificationsStep;

    @Mock
    private ProcessStepExecutionContext<ProcessConfig> stepContext;

    private static final UUID MODIFICATION_UUID = UUID.randomUUID();
    private static final UUID MISSING_MODIFICATION_UUID = UUID.randomUUID();
    private static final UUID MODIFICATION_UUID_2 = UUID.randomUUID();
    private static final UUID MODIFICATION_UUID_3 = UUID.randomUUID();

    private ReportNode reportNode;

    @BeforeEach
    void setUp() {
        applyModificationsStep = new ApplyModificationsStep<>(networkModificationService, networkModificationRestClient, s3Service, filterService);
        when(config.modifications()).thenReturn(List.of(new ModificationInfo(MODIFICATION_UUID, "descr", true)));
        when(stepContext.getConfig()).thenReturn(config);
        reportNode = ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build();
    }

    @Test
    void executeApplyModifications() {
        String stepType = applyModificationsStep.getType().getName();
        assertEquals("APPLY_MODIFICATIONS", stepType);

        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());
        NetworkModificationsWithMissingInfo networkModificationsWithMissingInfo = new NetworkModificationsWithMissingInfo(modificationInfos, List.of());

        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(stepContext.getReportNode()).thenReturn(reportNode);
        when(networkModificationRestClient.getModifications(any(List.class))).thenReturn(networkModificationsWithMissingInfo);
        doNothing().when(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        applyModificationsStep.execute(stepContext);
        verify(networkModificationRestClient).getModifications(any(List.class));
        verify(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));
    }

    @Test
    void executeApplyModificationsFailWhenModificationsAreMissing() {
        when(config.modifications()).thenReturn(List.of(
            new ModificationInfo(MODIFICATION_UUID, "descr1", true),
            new ModificationInfo(MISSING_MODIFICATION_UUID, "descr2", true)));

        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());
        NetworkModificationsWithMissingInfo networkModificationsWithMissingInfo = new NetworkModificationsWithMissingInfo(modificationInfos, List.of(MISSING_MODIFICATION_UUID));

        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(stepContext.getReportNode()).thenReturn(reportNode);
        when(networkModificationRestClient.getModifications(any(List.class))).thenReturn(networkModificationsWithMissingInfo);

        assertThrows(PowsyblException.class, () -> applyModificationsStep.execute(stepContext), "Some network composite modifications are missing");
        verify(networkModificationRestClient).getModifications(any(List.class));
        verify(networkModificationService, never()).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));
    }

    @Test
    void executeDoesNothingWhenModificationsEmpty() {
        when(config.modifications()).thenReturn(Collections.emptyList());

        applyModificationsStep.execute(stepContext);

        verifyNoInteractions(networkModificationService);
        verifyNoInteractions(networkModificationRestClient);
        verifyNoInteractions(filterService);
        verifyNoInteractions(s3Service);
    }

    @Test
    void executeApplyModificationsWhenModificationsAreNotApplied() {
        List<ModificationInfos> modificationInfos = List.of(
            LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build(),
            LoadModificationInfos.builder().equipmentId("load2").p0(new AttributeModification<>(500., OperationType.SET)).build(),
            LoadModificationInfos.builder().equipmentId("load3").q0(new AttributeModification<>(200., OperationType.SET)).build());
        NetworkModificationsWithMissingInfo networkModificationsWithMissingInfo = new NetworkModificationsWithMissingInfo(modificationInfos, List.of());

        when(config.modifications()).thenReturn(List.of(
            new ModificationInfo(MODIFICATION_UUID, "descr1", true),
            new ModificationInfo(MODIFICATION_UUID_2, "descr2", false),  // this modification will not be applied
            new ModificationInfo(MODIFICATION_UUID_3, "descr3", true)));

        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(stepContext.getReportNode()).thenReturn(reportNode);
        when(networkModificationRestClient.getModifications(any(List.class))).thenReturn(networkModificationsWithMissingInfo);
        doNothing().when(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        applyModificationsStep.execute(stepContext);
        verify(networkModificationRestClient).getModifications(any(List.class));
        verify(networkModificationService).applyModifications(any(Network.class), any(List.class), any(ReportNode.class), any(FilterService.class));

        // verify report added for modification MODIFICATION_UUID_2 not applied
        ReportNode applyReportNode = reportNode.getChildren().getFirst();
        assertEquals("monitor.worker.server.modifications.not.applied", applyReportNode.getMessageKey());
        assertEquals("Some network composite modifications are not applied intentionally (see process-config) : " + MODIFICATION_UUID_2,
            applyReportNode.getMessage());
    }

    @Test
    void executeApplyModificationsWhenAllModificationsAreInactive() {
        // all modifications are inactive
        when(config.modifications()).thenReturn(List.of(
            new ModificationInfo(MODIFICATION_UUID, "descr1", false),
            new ModificationInfo(MODIFICATION_UUID_2, "descr2", false),
            new ModificationInfo(MODIFICATION_UUID_3, "descr3", false)));

        when(stepContext.getReportNode()).thenReturn(reportNode);

        applyModificationsStep.execute(stepContext);

        verifyNoInteractions(networkModificationService);
        verifyNoInteractions(networkModificationRestClient);
        verifyNoInteractions(filterService);
        verifyNoInteractions(s3Service);

        // verify report added for all modifications not applied
        ReportNode applyReportNode = reportNode.getChildren().getFirst();
        assertEquals("monitor.worker.server.modifications.not.applied", applyReportNode.getMessageKey());
        assertEquals("Some network composite modifications are not applied intentionally (see process-config) : " +
                Stream.of(MODIFICATION_UUID, MODIFICATION_UUID_2, MODIFICATION_UUID_3).map(UUID::toString).collect(java.util.stream.Collectors.joining(", ")),
            applyReportNode.getMessage());
    }

    @Test
    void executeApplyModificationsDebugOn() throws IOException {
        String stepType = applyModificationsStep.getType().getName();
        assertEquals("APPLY_MODIFICATIONS", stepType);

        List<ModificationInfos> modificationInfos = List.of(LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build());
        NetworkModificationsWithMissingInfo networkModificationsWithMissingInfo = new NetworkModificationsWithMissingInfo(modificationInfos, List.of());

        Network network = mock(Network.class);
        when(stepContext.getNetwork()).thenReturn(network);
        when(stepContext.getReportNode()).thenReturn(reportNode);
        when(networkModificationRestClient.getModifications(any(List.class))).thenReturn(networkModificationsWithMissingInfo);
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
}
