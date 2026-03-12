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
import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.monitor.worker.server.dto.ReportInfos;
import org.gridsuite.monitor.worker.server.services.external.adapter.NetworkConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class LoadNetworkStepTest {

    @Mock
    private NetworkConversionService networkConversionService;

    private LoadNetworkStep<ProcessConfig> loadNetworkStep;

    @Mock
    private ProcessStepExecutionContext<ProcessConfig> stepContext;

    private static final UUID CASE_UUID = UUID.randomUUID();
    private static final UUID REPORT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        loadNetworkStep = new LoadNetworkStep<>(networkConversionService);
        when(stepContext.getCaseUuid()).thenReturn(CASE_UUID);
        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build());
        when(stepContext.getReportInfos()).thenReturn(reportInfos);
    }

    @Test
    void executeLoadNetwork() {
        Network expectedNetwork = EurostagTutorialExample1Factory.create();
        when(networkConversionService.createNetwork(eq(CASE_UUID), any(ReportNode.class)))
            .thenReturn(expectedNetwork);

        loadNetworkStep.execute(stepContext);

        String stepType = loadNetworkStep.getType().getName();
        assertEquals("LOAD_NETWORK", stepType);
        verify(networkConversionService).createNetwork(eq(CASE_UUID), any(ReportNode.class));
        verify(stepContext).setNetwork(expectedNetwork);
        ReportNode stepReportNode = stepContext.getReportInfos().reportNode();
        ReportNode importReportNode = stepReportNode.getChildren().getFirst();
        assertEquals("monitor.worker.server.importCase", importReportNode.getMessageKey());
    }
}
