/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.external.adapter;

import com.powsybl.cases.datasource.CaseDataSourceClient;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.gridsuite.monitor.worker.server.services.external.client.CaseRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class NetworkConversionServiceTest {

    @Mock
    private CaseRestClient caseRestClient;

    @Mock
    private CaseDataSourceClient caseDataSourceClient;

    @Mock
    private Importer importer;

    @Mock
    private Network network;

    @Mock
    private ReportNode reportNode;

    @InjectMocks
    private NetworkConversionService networkConversionService;

    private UUID caseUuid;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
    }

    @Test
    void createNetworkShouldImportNetworkWhenImporterFound() {
        when(caseRestClient.getCaseDataSource(caseUuid)).thenReturn(caseDataSourceClient);

        try (MockedStatic<Importer> importerMock = mockStatic(Importer.class)) {
            importerMock.when(() -> Importer.find(eq(caseDataSourceClient), any()))
                    .thenReturn(importer);
            when(importer.importData(
                    eq(caseDataSourceClient),
                    any(NetworkFactory.class),
                    any(Properties.class),
                    eq(reportNode)
            )).thenReturn(network);

            Network result = networkConversionService.createNetwork(caseUuid, reportNode);

            assertThat(result).isSameAs(network);
            verify(caseRestClient).getCaseDataSource(caseUuid);
            verify(importer).importData(
                    eq(caseDataSourceClient),
                    any(NetworkFactory.class),
                    any(Properties.class),
                    eq(reportNode)
            );
        }
    }

    @Test
    void createNetworkShouldThrowExceptionWhenNoImporterFound() {
        when(caseRestClient.getCaseDataSource(caseUuid)).thenReturn(caseDataSourceClient);

        try (MockedStatic<Importer> importerMock = mockStatic(Importer.class)) {
            importerMock.when(() -> Importer.find(eq(caseDataSourceClient), any(ComputationManager.class))).thenReturn(null);

            assertThatThrownBy(() -> networkConversionService.createNetwork(caseUuid, reportNode))
                    .isInstanceOf(PowsyblException.class)
                    .hasMessage("No importer found");
        }
    }
}
