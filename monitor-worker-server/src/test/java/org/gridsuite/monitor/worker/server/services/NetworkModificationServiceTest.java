/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.gridsuite.modification.dto.AttributeModification;
import org.gridsuite.modification.dto.LoadModificationInfos;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.dto.OperationType;
import org.gridsuite.modification.modifications.AbstractModification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Franck Lecuyer <franck.lecuyerat rte-france.com>
 */
@RestClientTest(NetworkModificationService.class)
class NetworkModificationServiceTest {
    @Autowired
    private NetworkModificationService networkModificationService;

    @Mock
    private FilterService filterService;

    @Autowired
    private MockRestServiceServer server;

    @Mock
    private Network network;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID MODIFICATION_1_UUID = UUID.fromString("11111111-7977-4592-ba19-88027e4254e4");
    private static final UUID MODIFICATION_2_UUID = UUID.fromString("22222222-7977-4592-ba19-88027e4254e4");
    private static final UUID MODIFICATION_ERROR_UUID = UUID.fromString("33333333-7977-4592-ba19-88027e4254e4");

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getModifications() {
        UUID[] modificationUuids = {MODIFICATION_1_UUID, MODIFICATION_2_UUID};

        ModificationInfos modificationInfos1 = LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build();
        ModificationInfos modificationInfos2 = LoadModificationInfos.builder().equipmentId("load2").q0(new AttributeModification<>(null, OperationType.UNSET)).build();

        ModificationInfos[] modificationInfos = {modificationInfos1, modificationInfos2};

        for (int i = 0; i < modificationUuids.length; i++) {
            ModificationInfos[] modificationsArray = {modificationInfos[i]};
            try {
                server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                    .andExpect(MockRestRequestMatchers.requestTo("http://network-modification-server/v1/network-composite-modification/" + modificationUuids[i] + "/network-modifications?onlyMetadata=false"))
                    .andRespond(MockRestResponseCreators.withSuccess()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(modificationsArray)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        List<ModificationInfos> resultListModifications = networkModificationService.getModifications(Arrays.asList(modificationUuids));
        assertThat(resultListModifications).hasSize(2);
    }

    @Test
    void getModificationsNotFound() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://network-modification-server/v1/network-composite-modification/" + MODIFICATION_ERROR_UUID + "/network-modifications?onlyMetadata=false"))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> networkModificationService.getModifications(List.of(MODIFICATION_ERROR_UUID))).isInstanceOf(PowsyblException.class);
    }

    @Test
    void applyModifications() {
        ModificationInfos modificationInfos1 = LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build();
        ModificationInfos modificationInfos2 = LoadModificationInfos.builder().equipmentId("load2").q0(new AttributeModification<>(null, OperationType.UNSET)).build();

        ModificationInfos[] modificationInfos = {modificationInfos1, modificationInfos2};
        ModificationInfos[] spyModifications = new ModificationInfos[modificationInfos.length];
        AbstractModification[] mockModifications = new AbstractModification[modificationInfos.length];
        for (int i = 0; i < modificationInfos.length; i++) {
            spyModifications[i] = spy(modificationInfos[i]);
            mockModifications[i] = mock(AbstractModification.class);
            when(spyModifications[i].toModification()).thenReturn(mockModifications[i]);
            doNothing().when(mockModifications[i]).check(any(Network.class));
            doNothing().when(mockModifications[i]).initApplicationContext(any(), any());
            doNothing().when(mockModifications[i]).apply(any(Network.class), any(ReportNode.class));
        }
        ReportNode mockReportNode = mock(ReportNode.class);

        assertThatNoException().isThrownBy(() -> networkModificationService.applyModifications(network, Arrays.asList(spyModifications), mockReportNode, filterService));

        for (int i = 0; i < modificationInfos.length; i++) {
            verify(mockModifications[i]).check(network);
            verify(mockModifications[i]).initApplicationContext(filterService, null);
            verify(mockModifications[i]).apply(network, mockReportNode);
        }
    }

    @Test
    void applyModificationsWithException() {
        ModificationInfos modificationInfos1 = LoadModificationInfos.builder().equipmentId("load1").q0(new AttributeModification<>(300., OperationType.SET)).build();
        ModificationInfos modificationInfos2 = LoadModificationInfos.builder().equipmentId("load2").q0(new AttributeModification<>(null, OperationType.UNSET)).build();

        ModificationInfos[] modificationInfos = {modificationInfos1, modificationInfos2};
        ModificationInfos[] spyModifications = new ModificationInfos[modificationInfos.length];
        AbstractModification[] mockModifications = new AbstractModification[modificationInfos.length];
        for (int i = 0; i < modificationInfos.length; i++) {
            spyModifications[i] = spy(modificationInfos[i]);
            mockModifications[i] = mock(AbstractModification.class);
            when(spyModifications[i].toModification()).thenReturn(mockModifications[i]);
            doThrow(new PowsyblException("Error in modification")).when(mockModifications[i]).check(any(Network.class));
        }
        ReportNode mockReportNode = mock(ReportNode.class);

        assertThatNoException().isThrownBy(() -> networkModificationService.applyModifications(network, Arrays.asList(spyModifications), mockReportNode, filterService));
        for (int i = 0; i < modificationInfos.length; i++) {
            verify(mockModifications[i]).check(network);
            verify(mockModifications[i], never()).initApplicationContext(filterService, null);
            verify(mockModifications[i], never()).apply(network, mockReportNode);
        }
    }
}
