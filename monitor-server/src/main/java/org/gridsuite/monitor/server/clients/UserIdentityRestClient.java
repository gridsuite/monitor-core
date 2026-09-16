/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import org.gridsuite.monitor.server.dto.useridentity.UserIdentities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class UserIdentityRestClient {
    private static final String USER_IDENTITY_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public UserIdentityRestClient(@Value("${gridsuite.services.user-identity-server.base-uri:http://user-identity-server/}") String userIdentityServerBaseUri,
                                  RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
            .baseUrl(userIdentityServerBaseUri + DELIMITER + USER_IDENTITY_API_VERSION)
            .build();
    }

    public UserIdentities getUserIdentities(List<String> userIds) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/users/identities")
                .queryParam("subs", String.join(",", userIds))
                .build())
            .retrieve()
            .body(UserIdentities.class);
    }
}
