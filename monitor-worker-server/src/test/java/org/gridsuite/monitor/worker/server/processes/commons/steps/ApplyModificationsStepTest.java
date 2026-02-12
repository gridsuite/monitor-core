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
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.ReportInfos;
import org.gridsuite.monitor.worker.server.services.FilterService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationRestService;
import org.gridsuite.monitor.worker.server.services.NetworkModificationService;
import org.gridsuite.monitor.worker.server.services.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private S3Service s3Service ;

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
}
