/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.services;

import com.powsybl.cases.datasource.CaseDataSourceClient;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetworkConversionServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Importer importer;

    @Mock
    private Network network;

    @Mock
    private ReportNode reportNode;

    private NetworkConversionService service;

    private UUID caseUuid;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        service = new NetworkConversionService("http://case-server/", restTemplate);
    }

    @Test
    void createNetworkShouldImportNetworkWhenImporterFound() {
        try (MockedStatic<Importer> importerMock = mockStatic(Importer.class)) {
            importerMock.when(() -> Importer.find(any(CaseDataSourceClient.class), any()))
                    .thenReturn(importer);

            when(importer.importData(
                    any(CaseDataSourceClient.class),
                    any(NetworkFactory.class),
                    any(Properties.class),
                    eq(reportNode)
            )).thenReturn(network);

            Network result = service.createNetwork(caseUuid, reportNode);

            assertThat(result).isSameAs(network);

            verify(importer).importData(
                    any(CaseDataSourceClient.class),
                    any(NetworkFactory.class),
                    any(Properties.class),
                    eq(reportNode)
            );
        }
    }

    @Test
    void createNetworkShouldThrowExceptionWhenNoImporterFound() {
        try (MockedStatic<Importer> importerMock = mockStatic(Importer.class)) {
            importerMock.when(() -> Importer.find(any(CaseDataSourceClient.class), any(ComputationManager.class))).thenReturn(null);

            assertThatThrownBy(() -> service.createNetwork(caseUuid, reportNode))
                    .isInstanceOf(PowsyblException.class)
                    .hasMessage("No importer found");
        }
    }
}
