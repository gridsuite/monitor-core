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
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.EnumExpertRule;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(FilterService.class)
class FilterServiceTest {
    @Autowired
    private FilterService filterService;

    @Autowired
    private MockRestServiceServer server;

    @Mock
    private Network network;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID FILTER_1_UUID = UUID.fromString("11111111-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_2_UUID = UUID.fromString("22222222-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_ERROR_UUID = UUID.fromString("33333333-7977-4592-ba19-88027e4254e4");

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getFilters() throws JsonProcessingException {
        List<UUID> filterUuids = List.of(FILTER_1_UUID, FILTER_2_UUID);

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(FILTER_1_UUID, new Date(), EquipmentType.GENERATOR, List.of(new IdentifierListFilterEquipmentAttributes("GEN", 1.0)));

        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule country1Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN).values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country1Filter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(FILTER_2_UUID, new Date(), EquipmentType.LINE, parentRule);

        List<AbstractFilter> listFilters = List.of(identifierListFilter, lineFilter);

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://filter-server/v1/filters/metadata?ids=" + filterUuids.stream().map(UUID::toString).collect(Collectors.joining(","))))
                .andRespond(MockRestResponseCreators.withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(listFilters)));

        List<AbstractFilter> resultListFilters = filterService.getFilters(filterUuids);
        assertThat(resultListFilters).hasSize(2);
    }

    @Test
    void getFiltersNotFound() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://filter-server/v1/filters/metadata?ids=" + FILTER_ERROR_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        List<UUID> filterUuids = List.of(FILTER_ERROR_UUID);
        assertThatThrownBy(() -> filterService.getFilters(filterUuids)).isInstanceOf(PowsyblException.class)
            .hasMessage("Error retrieving filters");
    }

    @Test
    void getUuidFilterEquipmentsMap() {
        FilterEquipments filterEquipments1 = mock(FilterEquipments.class);
        FilterEquipments filterEquipments2 = mock(FilterEquipments.class);

        when(filterEquipments1.getFilterId()).thenReturn(FILTER_1_UUID);
        when(filterEquipments1.getIdentifiableAttributes()).thenReturn(List.of(new IdentifiableAttributes("gen1", IdentifiableType.GENERATOR, 10.), new IdentifiableAttributes("gen2", IdentifiableType.GENERATOR, 20.)));
        when(filterEquipments1.getNotFoundEquipments()).thenReturn(List.of());

        when(filterEquipments2.getFilterId()).thenReturn(FILTER_2_UUID);
        when(filterEquipments2.getIdentifiableAttributes()).thenReturn(List.of(new IdentifiableAttributes("load1", IdentifiableType.LOAD, 10.), new IdentifiableAttributes("load2", IdentifiableType.LOAD, 20.)));
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

            filterServiceUtilsMock.verify(() -> FilterServiceUtils.getFilterEquipmentsFromUuid(eq(network), eq(new ArrayList<>(filters.keySet())), any()));
        }
    }
}
