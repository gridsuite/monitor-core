/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.gridsuite.monitor.server.services.result.ResultQueryParams;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
public final class ResultUriBuilder {
    private ResultUriBuilder() {
    }

    public static URI build(
            UriBuilder uriBuilder,
            String path,
            UUID resultUuid,
            String resultType,
            ResultQueryParams queryParams,
            boolean includePaged,
            boolean includePageAndSize
    ) {
        uriBuilder.path(path);

        if (resultType != null) {
            uriBuilder.queryParam("type", resultType);
        }
        if (includePaged) {
            uriBuilder.queryParam("paged", true);
        }
        if (includePageAndSize) {
            if (queryParams.page() != null) {
                uriBuilder.queryParam("page", queryParams.page());
            }
            if (queryParams.size() != null) {
                uriBuilder.queryParam("size", queryParams.size());
            }
        }

        queryParams.sort().forEach(sort -> uriBuilder.queryParam("sort", sort));

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("resultUuid", resultUuid);

        if (queryParams.filters() != null) {
            uriBuilder.queryParam("filters", "{filters}");
            uriVariables.put("filters", queryParams.filters());
        }

        return uriBuilder.build(uriVariables);
    }
}