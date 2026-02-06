/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.external.adapter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.gridsuite.modification.dto.AttributeModification;
import org.gridsuite.modification.dto.LoadModificationInfos;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.dto.OperationType;
import org.gridsuite.modification.modifications.AbstractModification;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(NetworkModificationService.class)
class NetworkModificationServiceTest {
    @Autowired
    private NetworkModificationService networkModificationService;

    @Mock
    private FilterService filterService;

    @Mock
    private Network network;

    @Test
    void applyModifications() {
        ModificationInfos modification1 = spy(
            LoadModificationInfos.builder()
                .equipmentId("load1")
                .q0(new AttributeModification<>(300., OperationType.SET))
                .build()
        );

        ModificationInfos modification2 = spy(
            LoadModificationInfos.builder()
                .equipmentId("load2")
                .q0(new AttributeModification<>(null, OperationType.UNSET))
                .build()
        );

        AbstractModification abstractModification1 = mock(AbstractModification.class);
        AbstractModification abstractModification2 = mock(AbstractModification.class);

        when(modification1.toModification()).thenReturn(abstractModification1);
        when(modification2.toModification()).thenReturn(abstractModification2);

        doNothing().when(abstractModification1).check(any());
        doNothing().when(abstractModification2).check(any());

        ReportNode reportNode = mock(ReportNode.class);

        networkModificationService.applyModifications(
            network,
            List.of(modification1, modification2),
            reportNode,
            filterService
        );

        verify(abstractModification1).check(network);
        verify(abstractModification1).initApplicationContext(filterService, null);
        verify(abstractModification1).apply(network, reportNode);

        verify(abstractModification2).check(network);
        verify(abstractModification2).initApplicationContext(filterService, null);
        verify(abstractModification2).apply(network, reportNode);
    }

    @Test
    void applyModificationsWithException() {
        ModificationInfos modification1 = spy(
            LoadModificationInfos.builder()
                .equipmentId("load1")
                .q0(new AttributeModification<>(300., OperationType.SET))
                .build()
        );

        ModificationInfos modification2 = spy(
            LoadModificationInfos.builder()
                .equipmentId("load2")
                .q0(new AttributeModification<>(null, OperationType.UNSET))
                .build()
        );

        AbstractModification abstractModification1 = mock(AbstractModification.class);
        AbstractModification abstractModification2 = mock(AbstractModification.class);

        when(modification1.toModification()).thenReturn(abstractModification1);
        when(modification2.toModification()).thenReturn(abstractModification2);

        doThrow(new PowsyblException("Error in modification"))
            .when(abstractModification1).check(any());

        doThrow(new PowsyblException("Error in modification"))
            .when(abstractModification2).check(any());

        ReportNode reportNode = mock(ReportNode.class);

        assertThatNoException().isThrownBy(() ->
            networkModificationService.applyModifications(
                network,
                List.of(modification1, modification2),
                reportNode,
                filterService
            )
        );

        verify(abstractModification1).check(network);
        verify(abstractModification1, never()).initApplicationContext(filterService, null);
        verify(abstractModification1, never()).apply(network, reportNode);

        verify(abstractModification2).check(network);
        verify(abstractModification2, never()).initApplicationContext(filterService, null);
        verify(abstractModification2, never()).apply(network, reportNode);
    }
}
