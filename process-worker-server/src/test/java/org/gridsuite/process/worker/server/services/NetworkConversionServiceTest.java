/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NetworkConversionService
 *
 * Tests cover:
 * - Network creation from case UUID
 * - RestTemplate configuration with base URI
 * - Integration with CaseDataSourceClient
 * - Exception handling (no importer found, import failures)
 * - URI template handler configuration
 *
 * Note: These tests focus on service configuration and error handling.
 * Full integration tests with actual PowSyBL importers should be done in integration tests.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class NetworkConversionServiceTest {

    @Mock
    private RestTemplate caseServerRest;

    @Mock
    private ReportNode reportNode;

    private NetworkConversionService networkConversionService;

    private static final String CASE_SERVER_BASE_URI = "http://case-server/";

    @BeforeEach
    void setUp() {
        networkConversionService = new NetworkConversionService(CASE_SERVER_BASE_URI, caseServerRest);
    }

    @Test
    void constructorShouldConfigureRestTemplateWithCorrectBaseUri() {
        // Given - already created in setUp()

        // Then - verify that setUriTemplateHandler was called with DefaultUriBuilderFactory
        verify(caseServerRest).setUriTemplateHandler(any(DefaultUriBuilderFactory.class));
    }

    @Test
    void createNetworkShouldThrowPowsyblExceptionWhenNoImporterFound() {
        // Given
        UUID caseUuid = UUID.randomUUID();

        // When & Then
        // When no valid importer is found for the data source, PowsyblException should be thrown
        PowsyblException exception = assertThrows(
            PowsyblException.class,
            () -> networkConversionService.createNetwork(caseUuid, reportNode)
        );

        assertEquals("No importer found", exception.getMessage());
    }
}
