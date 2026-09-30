/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.server.dto.report.MatchPosition;
import org.gridsuite.monitor.server.dto.report.Report;
import org.gridsuite.monitor.server.dto.report.ReportLog;
import org.gridsuite.monitor.server.dto.report.ReportPage;
import org.gridsuite.monitor.server.dto.report.Severity;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(ReportRestClient.class)
@ContextConfiguration(classes = {ReportRestClient.class})
class ReportRestClientTest {
    @Autowired
    private ReportRestClient reportRestClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getLogs() throws JsonProcessingException {
        UUID reportId = UUID.randomUUID();
        Set<String> severities = Set.of("INFO", "ERROR");

        ReportPage reportPage = new ReportPage(1, List.of(
            new ReportLog("message1", Severity.INFO, 1, UUID.randomUUID()),
            new ReportLog("message2", Severity.WARN, 2, UUID.randomUUID())), 100, 10);

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(request -> assertThat(request.getURI().getPath())
                    .isEqualTo("/v1/reports/" + reportId + "/logs"))
            .andExpect(MockRestRequestMatchers.queryParam("severityLevels", "INFO", "ERROR"))
            .andExpect(MockRestRequestMatchers.queryParam("paged", "true"))
            .andExpect(MockRestRequestMatchers.queryParam("page", "2"))
            .andExpect(MockRestRequestMatchers.queryParam("size", "10"))
            .andExpect(MockRestRequestMatchers.queryParam("message", "filter"))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(reportPage)));

        ReportPage reportResult = reportRestClient.getLogs(reportId, "filter", severities, 2, 10);
        assertThat(reportResult).usingRecursiveComparison().isEqualTo(reportPage);
    }

    @Test
    void getLogsSearch() throws JsonProcessingException {
        UUID reportId = UUID.randomUUID();
        List<MatchPosition> matches = List.of(new MatchPosition(1, 4), new MatchPosition(2, 0));

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId + "/logs/search?severityLevels=WARN&searchTerm=term&pageSize=20&message=filter"))
                .andRespond(MockRestResponseCreators.withSuccess()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(matches)));

        assertThat(reportRestClient.getLogsSearch(reportId, "filter", Set.of("WARN"), "term", 20))
                .usingRecursiveComparison().isEqualTo(matches);
    }

    @Test
    void getReport() throws JsonProcessingException {
        UUID reportId = UUID.randomUUID();
        Report report = new Report(reportId, null, "root", Severity.INFO, 0, List.of());

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId))
                .andRespond(MockRestResponseCreators.withSuccess()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(report)));

        assertThat(reportRestClient.getReport(reportId)).usingRecursiveComparison().isEqualTo(report);
    }

    @Test
    void getReportSeverities() throws JsonProcessingException {
        UUID reportId = UUID.randomUUID();
        Set<String> severities = Set.of("INFO", "ERROR");

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId + "/aggregated-severities"))
                .andRespond(MockRestResponseCreators.withSuccess()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(severities)));

        assertThat(reportRestClient.getReportSeverities(reportId)).containsExactlyInAnyOrder("INFO", "ERROR");
    }

    @Test
    void getLogsFailed() {
        UUID reportId = UUID.randomUUID();

        Set<String> severities = Set.of("INFO");

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId + "/logs?severityLevels=INFO&paged=true&page=0&size=10"))
                .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> reportRestClient.getLogs(reportId, null, severities, 0, 10))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    void deleteReport() {
        UUID reportId = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId))
            .andRespond(MockRestResponseCreators.withSuccess());

        assertThatNoException().isThrownBy(() -> reportRestClient.deleteReport(reportId));
    }

    @Test
    void deleteReportFailed() {
        UUID reportId = UUID.randomUUID();

        server.expect(MockRestRequestMatchers.method(HttpMethod.DELETE))
            .andExpect(MockRestRequestMatchers.requestTo("http://report-server/v1/reports/" + reportId))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> reportRestClient.deleteReport(reportId)).isInstanceOf(RestClientException.class);
    }
}
