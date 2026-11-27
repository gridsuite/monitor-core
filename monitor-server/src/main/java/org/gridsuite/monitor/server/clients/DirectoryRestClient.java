package org.gridsuite.monitor.server.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DirectoryRestClient {
    static final String DIRECTORY_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestClient restClient;

    public DirectoryRestClient(
        RestClient.Builder restClientBuilder,
        @Value("${gridsuite.services.directory-server.base-uri:http://directory-server/}") String exploreServerBaseUri) {
        this.restClient = restClientBuilder
            .baseUrl(exploreServerBaseUri + DELIMITER + DIRECTORY_API_VERSION)
            .build();
    }

    public Map<UUID, String> getElementNames(List<UUID> elementUuids) {
        return restClient.get()
            .uri(uriBuilder ->
                uriBuilder.path("/elements/names")
                    .queryParam("ids", elementUuids)
                    .build())
            .retrieve()
            .body(new ParameterizedTypeReference<>() { });

    }
}
