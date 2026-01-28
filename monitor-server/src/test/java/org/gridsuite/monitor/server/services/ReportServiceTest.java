/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.server.dto.Report;
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
import static org.assertj.core.api.Assertions.assertThatNoException;
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
        UUID child1ReportId = UUID.randomUUID();
        UUID child2ReportId = UUID.randomUUID();

        Report child1Report = Report.builder().id(child1ReportId).message("message1").severity(Severity.INFO).build();
        Report child2Report = Report.builder().id(child2ReportId).message("message2").severity(Severity.WARN).build();
        Report report = Report.builder().id(reportId).message("message3").subReports(List.of(child1Report, child2Report)).build();

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(report)));

        Report reportResult = reportService.getReport(reportId);
        assertThat(reportResult).usingRecursiveComparison().isEqualTo(report);
    }

    @Test
    void getReportFailed() {
        UUID reportId = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> reportService.getReport(reportId)).isInstanceOf(RestClientException.class);
    }

    @Test
    void deleteReport() {
        UUID reportId = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId))
            .andRespond(MockRestResponseCreators.withSuccess());

        assertThatNoException().isThrownBy(() -> reportService.deleteReport(reportId));
    }

    @Test
    void deleteReportFailed() {
        UUID reportId = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> reportService.deleteReport(reportId)).isInstanceOf(RestClientException.class);
    }
}
