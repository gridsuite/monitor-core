/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.automaticprocessconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.automaticprocessconfig.AutomaticProcessConfigRequest;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigEntity;
import org.gridsuite.monitor.server.entities.automaticprocessconfig.AutomaticProcessConfigHistoryEntity;
import org.gridsuite.monitor.server.entities.processconfig.AbstractProcessConfigEntity;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.repositories.automaticprocessconfig.AutomaticProcessConfigHistoryRepository;
import org.gridsuite.monitor.server.repositories.automaticprocessconfig.AutomaticProcessConfigRepository;
import org.gridsuite.monitor.server.repositories.processconfig.ProcessConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.DIFFERENT_PROCESS_CONFIG_TYPE;

/**
 * @author Radouane Khouadri <redouane.khouadri_externe at rte-france.com>
 */
@Service
public class AutomaticProcessConfigTxService {
    private final AutomaticProcessConfigRepository repository;
    private final AutomaticProcessConfigHistoryRepository historyRepository;
    private final ProcessConfigRepository processConfigRepository;

    public AutomaticProcessConfigTxService(
        AutomaticProcessConfigRepository repository,
        AutomaticProcessConfigHistoryRepository historyRepository,
        ProcessConfigRepository processConfigRepository
    ) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.processConfigRepository = processConfigRepository;
    }

    @Transactional
    public Optional<UUID> update(AutomaticProcessConfigRequest request, String user) {
        Optional<AbstractProcessConfigEntity> processConfig = processConfigRepository.findById(request.configUuid());
        if (processConfig.isEmpty()) {
            return Optional.empty();
        }

        ProcessType actualType = processConfig.get().getProcessType();
        if (actualType != request.processType()) {
            throw new MonitorServerException(DIFFERENT_PROCESS_CONFIG_TYPE, "Process config type does not match the requested process type",
                Map.of("processConfigType", actualType, "requestedProcessType", request.processType()));
        }

        Optional<AutomaticProcessConfigEntity> existing = repository.findById(request.processType());
        if (existing.filter(entity -> entity.getProcessConfig().getId().equals(request.configUuid())).isPresent()) {
            return Optional.of(request.configUuid());
        }

        AutomaticProcessConfigEntity entity = existing.orElseGet(AutomaticProcessConfigEntity::new);
        entity.setProcessType(request.processType());
        entity.setProcessConfig(processConfig.get());
        repository.save(entity);

        AutomaticProcessConfigHistoryEntity history = new AutomaticProcessConfigHistoryEntity();
        history.setProcessType(request.processType());
        history.setProcessConfig(processConfig.get());
        history.setChangedAt(Instant.now());
        history.setChangedBy(user);
        historyRepository.save(history);
        return Optional.of(request.configUuid());
    }

    @Transactional(readOnly = true)
    public Optional<UUID> get(ProcessType processType) {
        return repository.findById(processType).map(entity -> entity.getProcessConfig().getId());
    }
}
