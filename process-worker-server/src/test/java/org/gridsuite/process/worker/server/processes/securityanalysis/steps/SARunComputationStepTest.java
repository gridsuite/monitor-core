/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;
import org.gridsuite.process.worker.server.dto.ReportInfos;
import org.gridsuite.process.worker.server.processes.securityanalysis.DummySecurityAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
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

    private SARunComputationStep runComputationStep;

    private static final UUID PARAMS_UUID = UUID.randomUUID();
    private static final UUID REPORT_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        runComputationStep = new SARunComputationStep(securityAnalysisService);

        when(stepContext.getConfig()).thenReturn(config);
        when(config.parametersUuid()).thenReturn(PARAMS_UUID);

        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build());
        when(stepContext.getReportInfos()).thenReturn(reportInfos);
    }

    @Test
    void executeRunSecurityAnalysis() {
        Network network = EurostagTutorialExample1Factory.create();
        when(stepContext.getNetwork()).thenReturn(network);
        when(config.contingencies()).thenReturn(List.of("NHV1_NHV2_1", "NHV1_NHV2_2"));

        runComputationStep.execute(stepContext);

        String stepType = runComputationStep.getType().getName();
        assertEquals("RUN_SA_COMPUTATION", stepType);
        verify(securityAnalysisService).saveResult(
                argThat(resultInfos ->
                        resultInfos.resultUUID() != null &&
                        resultInfos.resultType().equals(ResultType.SECURITY_ANALYSIS)
                ),
                any(SecurityAnalysisReport.class)
        );
        verify(stepContext).setResultInfos(argThat(resultInfos ->
                        resultInfos.resultUUID() != null &&
                        resultInfos.resultType() == ResultType.SECURITY_ANALYSIS
        ));
    }
}
