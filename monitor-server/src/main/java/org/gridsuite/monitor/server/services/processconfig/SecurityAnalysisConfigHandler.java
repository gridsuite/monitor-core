/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.processconfig.SecurityAnalysisProcessConfigRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Component
public class SecurityAnalysisConfigHandler implements ProcessConfigHandler<SecurityAnalysisConfig, SecurityAnalysisConfigEntity> {

    private final SecurityAnalysisConfigMapper mapper;
    private final SecurityAnalysisProcessConfigRepository repository;

    public SecurityAnalysisConfigHandler(SecurityAnalysisConfigMapper mapper,
                                         SecurityAnalysisProcessConfigRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.SECURITY_ANALYSIS;
    }

    @Override
    public SecurityAnalysisConfigEntity toEntity(SecurityAnalysisConfig config) {
        return mapper.toEntity(config);
    }

    @Override
    public SecurityAnalysisConfig toDto(SecurityAnalysisConfigEntity entity) {
        return mapper.toDto(entity);
    }

    @Override
    public void updateEntity(SecurityAnalysisConfig config, SecurityAnalysisConfigEntity entity) {
        mapper.updateEntityFromDto(config, entity);
    }

    @Override
    public List<SecurityAnalysisConfigEntity> findAll() {
        return repository.findAll();
    }
}
