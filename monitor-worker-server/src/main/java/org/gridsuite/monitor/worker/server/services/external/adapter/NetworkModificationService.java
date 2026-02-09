/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.monitor.worker.server.services.external.adapter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import org.gridsuite.modification.NetworkModificationException;
import org.gridsuite.modification.dto.ModificationInfos;
import org.gridsuite.modification.modifications.AbstractModification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class NetworkModificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkModificationService.class);

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
