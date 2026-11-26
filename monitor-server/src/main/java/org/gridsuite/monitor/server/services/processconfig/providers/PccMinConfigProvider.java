/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig.providers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gridsuite.monitor.commons.types.processconfig.PccMinConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.PccMinConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.PccMinConfigMapper;
import org.gridsuite.monitor.server.repositories.processconfig.PccMinConfigRepository;
import org.springframework.stereotype.Service;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
@Service
@Getter
@RequiredArgsConstructor
public class PccMinConfigProvider implements ProcessConfigProvider<
    PccMinConfig,
    PccMinConfigEntity> {

    private final PccMinConfigMapper mapper;
    private final PccMinConfigRepository repository;

    @Override
    public ProcessType getProcessType() {
        return ProcessType.PCC_MIN;
    }
}
