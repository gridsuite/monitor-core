/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.monitor.server.dto.useridentity.UserIdentities;
import org.gridsuite.monitor.server.dto.useridentity.UserIdentity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestClientTest(UserIdentityRestClient.class)
@ContextConfiguration(classes = {UserIdentityRestClient.class})
class UserIdentityRestClientTest {
    @Autowired
    private UserIdentityRestClient userIdentityRestClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getUserIdentities() throws JsonProcessingException {
        Map<String, UserIdentity> userIdentitiesMap = new HashMap<>();
        userIdentitiesMap.put("user1", new UserIdentity("user1", "titi", "tutu"));
        userIdentitiesMap.put("user2", new UserIdentity("user2", "toto", "tata"));
        UserIdentities userIdentities = new UserIdentities(userIdentitiesMap);

        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestToUriTemplate("http://user-identity-server/v1/users/identities?subs={subs}", "user1,user2"))
            .andRespond(MockRestResponseCreators.withSuccess()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(userIdentities)));

        UserIdentities userIdentitiesResult = userIdentityRestClient.getUserIdentities(List.of("user1", "user2"));
        assertThat(userIdentitiesResult).usingRecursiveComparison().isEqualTo(userIdentities);
    }

    @Test
    void getUserIdentitiesFailed() {
        server.expect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(MockRestRequestMatchers.requestToUriTemplate("http://user-identity-server/v1/users/identities?subs={subs}", "user1,user2"))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> userIdentityRestClient.getUserIdentities(List.of("user1", "user2"))).isInstanceOf(RestClientException.class);
    }

}
