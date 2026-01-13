/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.worker.server.services;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.gridsuite.modification.NetworkModificationException;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.modifications.AbstractModification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class NetworkModificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkModificationService.class);
    private static final String NETWORK_MODIFICATION_SERVER_API_VERSION = "v1";
    private static final String DELIMITER = "/";

    private final RestTemplate networkModificationServerRest;
    private final String networkModificationServerBaseUri;

    public NetworkModificationService(@Value("${gridsuite.services.network-modification-server.base-uri:http://network-modification-server/}") String networkModificationServerBaseUri,
                                      RestTemplateBuilder restTemplateBuilder) {
        this.networkModificationServerRest = restTemplateBuilder.build();
        this.networkModificationServerBaseUri = networkModificationServerBaseUri;
    }

    public List<ModificationInfos> getModifications(List<UUID> modificationsUuids) {
        List<ModificationInfos> modifications = new ArrayList<>();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromUriString(DELIMITER + NETWORK_MODIFICATION_SERVER_API_VERSION + DELIMITER +
                "network-composite-modification" + DELIMITER + "{uuid}" + DELIMITER + "network-modifications?onlyMetadata=false");

        modificationsUuids.forEach(uuid -> {
            String path = this.networkModificationServerBaseUri + uriBuilder.buildAndExpand(uuid).toUriString();
            try {
                ModificationInfos[] modificationInfos = networkModificationServerRest.getForObject(path, ModificationInfos[].class);
                if (modificationInfos != null) {
                    modifications.addAll(Arrays.asList(modificationInfos));
                }
            } catch (HttpStatusCodeException e) {
                throw new PowsyblException("Error retrieving modifications", e);
            }
        });

        return modifications;
    }

    public void applyModifications(Network network, List<ModificationInfos> modificationsInfos, ReportNode reportNode, FilterService filterService) {
        modificationsInfos.stream()
            .filter(ModificationInfos::getActivated)
            .forEach(modificationInfos -> {
                try {
                    AbstractModification modification = modificationInfos.toModification();
                    modification.check(network);
                    modification.initApplicationContext(filterService, null);
                    modification.apply(network, reportNode);
                } catch (Exception e) {
                    // For now, we just log the error, and we continue to apply the following modifications
                    handleException(modificationInfos.getErrorType(), e);
                }
            });
    }

    private void handleException(NetworkModificationException.Type typeIfError, Exception e) {
        boolean isApplicationException = PowsyblException.class.isAssignableFrom(e.getClass());
        if (!isApplicationException) {
            LOGGER.error("{}", e.getMessage(), e);
        } else {
            LOGGER.error("{} : {}", typeIfError.name(), e.getMessage(), e);
        }
    }
}
