/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services.external.adapter;

import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.modification.IFilterService;
import org.gridsuite.modification.dto.FilterEquipments;
import org.gridsuite.modification.dto.IdentifiableAttributes;
import org.gridsuite.monitor.worker.server.services.external.client.FilterRestClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class FilterService implements IFilterService {
    private final FilterRestClient filterRestClient;

    public FilterService(FilterRestClient filterRestClient) {
        this.filterRestClient = filterRestClient;
    }

    public List<AbstractFilter> getFilters(List<UUID> filtersUuids) {
        return filterRestClient.getFilters(filtersUuids);
    }

    public Stream<org.gridsuite.filter.identifierlistfilter.FilterEquipments> exportFilters(List<UUID> filtersUuids, Network network) {
        return FilterServiceUtils.getFilterEquipmentsFromUuid(network, filtersUuids, this::getFilters).stream();
    }

    public Map<UUID, FilterEquipments> getUuidFilterEquipmentsMap(Network network, Map<UUID, String> filters) {
        return exportFilters(new ArrayList<>(filters.keySet()), network)
            .map(f -> new FilterEquipments(f.getFilterId(), filters.get(f.getFilterId()),
                f.getIdentifiableAttributes().stream().map(i -> new IdentifiableAttributes(i.getId(), i.getType(), i.getDistributionKey())).toList(),
                f.getNotFoundEquipments()))
            .collect(Collectors.toMap(FilterEquipments::getFilterId, Function.identity()));
    }
}
