/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class FilterServiceTest {
    @InjectMocks
    private FilterService filterService;

    @Mock
    private Network network;

    private static final UUID FILTER_1_UUID = UUID.fromString("11111111-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_2_UUID = UUID.fromString("22222222-7977-4592-ba19-88027e4254e4");

    @Test
    void getUuidFilterEquipmentsMap() {
        FilterEquipments filterEquipments1 = mock(FilterEquipments.class);
        FilterEquipments filterEquipments2 = mock(FilterEquipments.class);
        List<IdentifiableAttributes> identifiableAttributesList1 = List.of(new IdentifiableAttributes("gen1", IdentifiableType.GENERATOR, 10.), new IdentifiableAttributes("gen2", IdentifiableType.GENERATOR, 20.));
        List<IdentifiableAttributes> identifiableAttributesList2 = List.of(new IdentifiableAttributes("load1", IdentifiableType.LOAD, 10.), new IdentifiableAttributes("load2", IdentifiableType.LOAD, 20.));

        when(filterEquipments1.getFilterId()).thenReturn(FILTER_1_UUID);
        when(filterEquipments1.getIdentifiableAttributes()).thenReturn(identifiableAttributesList1);
        when(filterEquipments1.getNotFoundEquipments()).thenReturn(List.of());

        when(filterEquipments2.getFilterId()).thenReturn(FILTER_2_UUID);
        when(filterEquipments2.getIdentifiableAttributes()).thenReturn(identifiableAttributesList2);
        when(filterEquipments2.getNotFoundEquipments()).thenReturn(List.of("load2"));

        List<FilterEquipments> mockFilterEquipmentsList = List.of(filterEquipments1, filterEquipments2);

        Map<UUID, String> filters = Map.of(FILTER_1_UUID, "Filter1", FILTER_2_UUID, "Filter2");

        try (MockedStatic<FilterServiceUtils> filterServiceUtilsMock = mockStatic(FilterServiceUtils.class)) {
            filterServiceUtilsMock.when(() -> FilterServiceUtils.getFilterEquipmentsFromUuid(any(Network.class), any(List.class), any()))
                .thenReturn(mockFilterEquipmentsList);

            Map<UUID, org.gridsuite.modification.dto.FilterEquipments> result = filterService.getUuidFilterEquipmentsMap(network, filters);

            assertThat(result).hasSize(2);
            assertThat(result).containsKey(FILTER_1_UUID);
            assertThat(result).containsKey(FILTER_2_UUID);
            assertThat(result.get(FILTER_1_UUID).getFilterName()).isEqualTo("Filter1");
            assertThat(result.get(FILTER_1_UUID).getIdentifiableAttributes()).usingRecursiveComparison().isEqualTo(identifiableAttributesList1);

            assertThat(result.get(FILTER_2_UUID).getFilterName()).isEqualTo("Filter2");
            assertThat(result.get(FILTER_2_UUID).getIdentifiableAttributes()).usingRecursiveComparison().isEqualTo(identifiableAttributesList2);

            filterServiceUtilsMock.verify(() -> FilterServiceUtils.getFilterEquipmentsFromUuid(eq(network), eq(new ArrayList<>(filters.keySet())), any()));
        }
    }
}
