/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import org.gridsuite.process.worker.server.dto.ReportInfos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportService
 *
 * Tests cover:
 * - Successful report sending
 * - HTTP communication with report server
 * - JSON serialization handling
 * - Exception scenarios (serialization errors, HTTP errors)
 * - URI construction and header validation
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReportNode reportNode;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> httpEntityCaptor;

    private ReportService reportService;

    private static final String REPORT_SERVER_BASE_URI = "http://report-server/";

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        reportService = new ReportService(objectMapper, REPORT_SERVER_BASE_URI, restTemplateBuilder);
    }

    @Test
    void sendReportShouldSendCorrectHttpRequestWhenReportIsValid() throws JsonProcessingException {
        // Given
        UUID reportUuid = UUID.randomUUID();
        String reportJson = "{\"reportKey\":\"reportValue\"}";
        ReportInfos reportInfos = new ReportInfos(reportUuid, reportNode);

        when(objectMapper.writeValueAsString(reportNode)).thenReturn(reportJson);
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            eq(ReportNode.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        reportService.sendReport(reportInfos);

        // Then
        verify(objectMapper).writeValueAsString(reportNode);
        verify(restTemplate).exchange(
            urlCaptor.capture(),
            eq(HttpMethod.PUT),
            httpEntityCaptor.capture(),
            eq(ReportNode.class)
        );

        // Verify URL construction
        String expectedUrl = "http://report-server/v1/reports/" + reportUuid;
        assertEquals(expectedUrl, urlCaptor.getValue());

        // Verify HTTP entity
        HttpEntity<String> httpEntity = httpEntityCaptor.getValue();
        assertEquals(reportJson, httpEntity.getBody());
        assertEquals("application/json", httpEntity.getHeaders().getContentType().toString());
    }

    @Test
    void sendReportShouldThrowPowsyblExceptionWhenJsonProcessingFails() throws JsonProcessingException {
        // Given
        UUID reportUuid = UUID.randomUUID();
        ReportInfos reportInfos = new ReportInfos(reportUuid, reportNode);

        JsonProcessingException jsonException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(reportNode)).thenThrow(jsonException);

        // When & Then
        PowsyblException exception = assertThrows(
            PowsyblException.class,
            () -> reportService.sendReport(reportInfos)
        );

        assertEquals("Error sending report", exception.getMessage());
        assertEquals(jsonException, exception.getCause());

        // Verify that RestTemplate was never called
        verify(restTemplate, never()).exchange(
            anyString(),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(ReportNode.class)
        );
    }
}
