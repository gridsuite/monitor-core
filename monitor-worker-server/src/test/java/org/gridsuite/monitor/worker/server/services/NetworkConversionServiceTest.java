/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@RestClientTest(NetworkConversionService.class)
class NetworkConversionServiceTest {
    @Autowired
    private NetworkConversionService service;

    @Mock
    private Network network;

    @Mock
    private ReportNode reportNode;

    private UUID caseUuid;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
    }

    @Test
    void createNetworkShouldReadNetwork() {
        try (MockedStatic<Network> networkMock = mockStatic(Network.class)) {
            networkMock.when(() -> Network.read(any(ReadOnlyDataSource.class), any(Properties.class), any(ReportNode.class)))
                    .thenReturn(network);

            Network result = service.createNetwork(caseUuid, reportNode);

            assertThat(result).isSameAs(network);
            networkMock.verify(() -> Network.read(any(ReadOnlyDataSource.class), any(Properties.class), any(ReportNode.class)));
        }
    }
}
