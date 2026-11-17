/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.filter.Filter;
import org.gridsuite.modification.context.dto.FilterWithDistributionKeys;
import org.gridsuite.monitor.worker.server.clients.FilterRestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class FilterServiceTest {
    @InjectMocks
    private FilterService filterService;

    @Mock
    private FilterRestClient filterRestClient;

    private static final UUID FILTER_1_UUID = UUID.fromString("11111111-7977-4592-ba19-88027e4254e4");

    @Test
    void getStandaloneFilters() {
        Map<UUID, Filter> filters = Map.of(FILTER_1_UUID, mock(Filter.class));
        when(filterRestClient.getStandaloneFilters(List.of(FILTER_1_UUID))).thenReturn(filters);

        assertThat(filterService.getStandaloneFilters(List.of(FILTER_1_UUID))).isSameAs(filters);
        verify(filterRestClient).getStandaloneFilters(List.of(FILTER_1_UUID));
    }

    @Test
    void getStandaloneFiltersWithDistributionKeys() {
        Map<UUID, FilterWithDistributionKeys> filters = Map.of(FILTER_1_UUID, mock(FilterWithDistributionKeys.class));
        when(filterRestClient.getStandaloneFiltersWithDistributionKeys(List.of(FILTER_1_UUID))).thenReturn(filters);

        assertThat(filterService.getStandaloneFiltersWithDistributionKeys(List.of(FILTER_1_UUID))).isSameAs(filters);
        verify(filterRestClient).getStandaloneFiltersWithDistributionKeys(List.of(FILTER_1_UUID));
    }
}
