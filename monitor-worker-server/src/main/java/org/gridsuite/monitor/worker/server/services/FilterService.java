/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.filter.wip.Filter;
import org.gridsuite.modification.context.dto.FilterWithDistributionKeys;
import org.gridsuite.monitor.worker.server.clients.FilterRestClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class FilterService {
    private final FilterRestClient filterRestClient;

    public FilterService(FilterRestClient filterRestClient) {
        this.filterRestClient = filterRestClient;
    }

    public Map<UUID, Filter> getStandaloneFilters(List<UUID> filtersUuids) {
        return filterRestClient.getStandaloneFilters(filtersUuids);
    }

    public Map<UUID, FilterWithDistributionKeys> getStandaloneFiltersWithDistributionKeys(List<UUID> filtersUuids) {
        return filterRestClient.getStandaloneFiltersWithDistributionKeys(filtersUuids);
    }

}
