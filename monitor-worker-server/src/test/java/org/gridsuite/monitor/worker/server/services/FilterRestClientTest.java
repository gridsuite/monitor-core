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
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.EnumExpertRule;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.monitor.worker.server.client.FilterRestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(FilterRestClient.class)
class FilterRestClientTest {
    @Autowired
    private FilterRestClient filterRestClient;

    @Autowired
    private MockRestServiceServer server;

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
        EnumExpertRule country1Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN).values(new HashSet<>(Set.of("FR"))).build();
        rules.add(country1Filter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(FILTER_2_UUID, new Date(), EquipmentType.LINE, parentRule);

        List<AbstractFilter> listFilters = List.of(identifierListFilter, lineFilter);

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andExpect(MockRestRequestMatchers.requestTo("http://filter-server/v1/filters/metadata?ids=" + FILTER_1_UUID + "&ids=" + FILTER_2_UUID))
                .andRespond(MockRestResponseCreators.withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(listFilters)));

        List<AbstractFilter> resultListFilters = filterRestClient.getFilters(filterUuids);
        assertThat(resultListFilters).usingRecursiveComparison().isEqualTo(listFilters);
    }

    @Test
    void getFiltersNotFound() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestTo("http://filter-server/v1/filters/metadata?ids=" + FILTER_ERROR_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        List<UUID> filterUuids = List.of(FILTER_ERROR_UUID);
        assertThatThrownBy(() -> filterRestClient.getFilters(filterUuids)).isInstanceOf(PowsyblException.class)
            .hasMessage("Error retrieving filters");
    }
}
