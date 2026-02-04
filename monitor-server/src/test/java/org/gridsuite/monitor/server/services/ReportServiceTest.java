/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.server.dto.ReportLog;
import org.gridsuite.monitor.server.dto.ReportPage;
import org.gridsuite.monitor.server.dto.Severity;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(ReportService.class)
@ContextConfiguration(classes = {ReportService.class})
class ReportServiceTest {
    @Autowired
    private ReportService reportService;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getReport() throws JsonProcessingException {
        UUID reportId = UUID.randomUUID();

        ReportPage reportPage = new ReportPage(1, List.of(
            new ReportLog("message1", Severity.INFO, 1, UUID.randomUUID()),
            new ReportLog("message2", Severity.WARN, 2, UUID.randomUUID())), 100, 10);

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId + "/logs"))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(reportPage)));

        ReportPage reportResult = reportService.getReport(reportId);
        assertThat(reportResult).usingRecursiveComparison().isEqualTo(reportPage);
    }

    @Test
    void getReportFailed() {
        UUID reportId = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId + "/logs"))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> reportService.getReport(reportId)).isInstanceOf(RestClientException.class);
    }
}
