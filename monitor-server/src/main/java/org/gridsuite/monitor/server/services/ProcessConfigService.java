/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.AbstractProcessConfigEntity;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class ProcessConfigService {
    private final ProcessConfigRepository processConfigRepository;

    public ProcessConfigService(ProcessConfigRepository processConfigRepository) {
        this.processConfigRepository = processConfigRepository;
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        switch (processConfig.processType()) {
            case SECURITY_ANALYSIS -> {
                return processConfigRepository.save(new SecurityAnalysisConfigEntity((SecurityAnalysisConfig) processConfig)).getId();
            }
            case null, default -> throw new IllegalArgumentException("Unsupported process config type: " + processConfig.processType());
        }
    }

    @Transactional(readOnly = true)
    public ProcessConfig getProcessConfig(UUID processConfigUuid) {
        AbstractProcessConfigEntity processConfigEntity = processConfigRepository.findById(processConfigUuid).orElse(null);
        if (processConfigEntity != null) {
            switch (processConfigEntity.getType()) {
                case SECURITY_ANALYSIS -> {
                    SecurityAnalysisConfigEntity securityAnalysisConfigEntity = (SecurityAnalysisConfigEntity) processConfigEntity;
                    return new SecurityAnalysisConfig(securityAnalysisConfigEntity.getParametersUuid(),
                                                      securityAnalysisConfigEntity.getContingencies(),
                                                      securityAnalysisConfigEntity.getModificationUuids());
                }
                case null, default ->
                    throw new IllegalArgumentException("Unsupported process config type: " + processConfigEntity.getType());
            }
        } else {
            return null;
        }
    }

    @Transactional
    public boolean updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        AbstractProcessConfigEntity processConfigEntity = processConfigRepository.findById(processConfigUuid).orElse(null);
        if (processConfigEntity != null) {
            if (processConfigEntity.getType() != processConfig.processType()) {
                throw new IllegalArgumentException("Process config type mismatch : " + processConfigEntity.getType());
            }

            processConfigEntity.setModificationUuids(processConfig.modificationUuids());

            switch (processConfigEntity.getType()) {
                case SECURITY_ANALYSIS -> {
                    SecurityAnalysisConfigEntity securityAnalysisConfigEntity = (SecurityAnalysisConfigEntity) processConfigEntity;
                    SecurityAnalysisConfig securityAnalysisConfig = (SecurityAnalysisConfig) processConfig;
                    securityAnalysisConfigEntity.setParametersUuid(securityAnalysisConfig.parametersUuid());
                    securityAnalysisConfigEntity.setContingencies(securityAnalysisConfig.contingencies());
                }
                case null, default ->
                    throw new IllegalArgumentException("Unsupported process config type: " + processConfigEntity.getType());
            }
            processConfigRepository.save(processConfigEntity);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean deleteProcessConfig(UUID processConfigUuid) {
        if (processConfigRepository.existsById(processConfigUuid)) {
            processConfigRepository.deleteById(processConfigUuid);
            return true;
        }
        return false;
    }
}
