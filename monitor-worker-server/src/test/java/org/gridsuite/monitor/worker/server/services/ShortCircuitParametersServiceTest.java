/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.shortcircuit.BusFault;
import com.powsybl.shortcircuit.Fault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
class ShortCircuitParametersServiceTest {

    private ShortCircuitParametersService shortCircuitParametersService;

    @Mock
    private Network network;

    @BeforeEach
    void setUp() {
        shortCircuitParametersService = new ShortCircuitParametersService();
    }

    @Test
    void getAllBusFaultsNoParameters() {
        Bus bus1 = mock(Bus.class);
        when(bus1.getId()).thenReturn("bus1");
        Bus bus2 = mock(Bus.class);
        when(bus2.getId()).thenReturn("bus2");

        Network.BusView busView = mock(Network.BusView.class);
        when(network.getBusView()).thenReturn(busView);
        when(busView.getBusStream()).thenReturn(Stream.of(bus1, bus2));

        List<Fault> faults = shortCircuitParametersService.getAllBusFaults(network, null);

        assertThat(faults).hasSize(2);
        assertThat(faults.get(0)).isInstanceOf(BusFault.class);
        assertThat((faults.get(0)).getId()).isEqualTo("bus1");
        assertThat((faults.get(1)).getId()).isEqualTo("bus2");
    }

    @Test
    void getAllBusFaultsWithNodeClusters() {
        Bus bus1 = mock(Bus.class);
        when(bus1.getId()).thenReturn("bus1");
        Bus bus2 = mock(Bus.class);
        when(bus2.getId()).thenReturn("bus2");

        Network.BusView busView = mock(Network.BusView.class);
        when(network.getBusView()).thenReturn(busView);
        when(busView.getBusStream()).thenReturn(Stream.of(bus1, bus2));

        Map<String, String> specificParameters = Map.of(ShortCircuitParametersService.NODE_CLUSTER, "bus1");

        List<Fault> faults = shortCircuitParametersService.getAllBusFaults(network, specificParameters);

        assertThat(faults).hasSize(1);
        assertThat((faults.get(0)).getId()).isEqualTo("bus1");
    }

    @Test
    void getAllBusFaultsWithMultipleNodeClusters() {
        Bus bus1 = mock(Bus.class);
        when(bus1.getId()).thenReturn("bus1");
        Bus bus2 = mock(Bus.class);
        when(bus2.getId()).thenReturn("bus2");
        Bus bus3 = mock(Bus.class);
        when(bus3.getId()).thenReturn("bus3");

        Network.BusView busView = mock(Network.BusView.class);
        when(network.getBusView()).thenReturn(busView);
        when(busView.getBusStream()).thenReturn(Stream.of(bus1, bus2, bus3));

        Map<String, String> specificParameters = Map.of(ShortCircuitParametersService.NODE_CLUSTER, "bus1, bus3");

        List<Fault> faults = shortCircuitParametersService.getAllBusFaults(network, specificParameters);

        assertThat(faults).hasSize(2);
        assertThat(faults.stream().map(Fault::getId)).containsExactlyInAnyOrder("bus1", "bus3");
    }

    @Test
    void checkInconsistentVoltageLevelsConsistent() {
        VoltageLevel vl = mock(VoltageLevel.class);
        IdentifiableShortCircuit extension = mock(IdentifiableShortCircuit.class);
        when(vl.getExtension(IdentifiableShortCircuit.class)).thenReturn(extension);
        when(extension.getIpMin()).thenReturn(10.0);
        when(extension.getIpMax()).thenReturn(20.0);

        when(network.getVoltageLevelStream()).thenReturn(Stream.of(vl));

        shortCircuitParametersService.checkInconsistentVoltageLevels(network);
    }

    @Test
    void checkInconsistentVoltageLevelsInconsistent() {
        VoltageLevel vl = mock(VoltageLevel.class);
        when(vl.getId()).thenReturn("vl1");
        IdentifiableShortCircuit extension = mock(IdentifiableShortCircuit.class);
        when(vl.getExtension(IdentifiableShortCircuit.class)).thenReturn(extension);
        when(extension.getIpMin()).thenReturn(30.0);
        when(extension.getIpMax()).thenReturn(20.0);

        when(network.getVoltageLevelStream()).thenReturn(Stream.of(vl));

        assertThatThrownBy(() -> shortCircuitParametersService.checkInconsistentVoltageLevels(network))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Some voltage levels have wrong isc values");
    }
}
