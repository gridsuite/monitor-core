/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig.providers;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.AbstractProcessConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.ProcessConfigMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
public interface ProcessConfigProvider<C extends ProcessConfig, E extends AbstractProcessConfigEntity> {

    ProcessType getProcessType();

    ProcessConfigMapper<C, E> getMapper();

    JpaRepository<E, UUID> getRepository();
}
