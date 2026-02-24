/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.report.ReportNode;
import org.gridsuite.monitor.commons.ReportInfos;
import org.gridsuite.monitor.worker.server.config.MonitorWorkerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RestClientTest(ReportService.class)
@ContextConfiguration(classes = {MonitorWorkerConfig.class, ReportService.class})
class ReportServiceTest {

    private static final UUID REPORT_UUID = UUID.randomUUID();
    private static final UUID REPORT_ERROR_UUID = UUID.randomUUID();

    @Autowired
    private ReportService reportService;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void sendReport() throws JsonProcessingException {
        final ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build();
        String expectedJson = objectMapper.writeValueAsString(reportNode);

        server.expect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + REPORT_UUID))
                .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockRestRequestMatchers.content().json(expectedJson))
                .andRespond(MockRestResponseCreators.withSuccess());

        ReportInfos reportInfos = new ReportInfos(REPORT_UUID, reportNode);
        assertThatNoException().isThrownBy(() -> reportService.sendReport(reportInfos));
    }

    @Test
    void sendReportFailed() {
        final ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles("i18n.reports")
                .withMessageTemplate("test")
                .build();

        server.expect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + REPORT_ERROR_UUID))
                .andRespond(MockRestResponseCreators.withServerError());

        ReportInfos reportInfos = new ReportInfos(REPORT_ERROR_UUID, reportNode);
        assertThatThrownBy(() -> reportService.sendReport(reportInfos)).isInstanceOf(RestClientException.class);
    }
}
