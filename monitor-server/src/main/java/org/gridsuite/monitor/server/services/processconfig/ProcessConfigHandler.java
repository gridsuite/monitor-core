/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;

import java.util.List;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
public interface ProcessConfigHandler<C extends ProcessConfig, E extends ProcessConfigEntity> {

    ProcessType getProcessType();

    void update(E entity, C processConfig);

    E copyEntity(E sourceEntity);

    E toEntity(C processConfig);

    C toProcessConfig(E entity);

    List<ProcessConfigFieldComparison> computeDifferences(E entity1, E entity2);
}
