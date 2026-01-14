/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@JsonTest
@ContextConfiguration(classes = {MonitorWorkerConfig.class})
class MonitorWorkerConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void jacksonCustomizerRegistersSecurityAnalysisModule() throws JsonProcessingException {
        SecurityAnalysisResult result = new SecurityAnalysisResult(
                new LimitViolationsResult(Collections.emptyList(), Collections.emptyList()),
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                Collections.emptyList()
        );

        String json = objectMapper.writeValueAsString(result);

        assertThat(json).isNotEmpty();
        assertThatNoException().isThrownBy(() -> objectMapper.readValue(json, SecurityAnalysisResult.class));
    }

    @Test
    void jacksonCustomizerRegistersReportNodeModule() throws JsonProcessingException {
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withResourceBundles("i18n.reports")
            .withMessageTemplate("test")
            .build();

        String json = objectMapper.writeValueAsString(reportNode);

        assertThat(json).isNotEmpty();
        assertThatNoException().isThrownBy(() -> objectMapper.readValue(json, ReportNode.class));
    }
}
