/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processexecution;

import org.gridsuite.monitor.server.config.MapStructConfig;
import org.gridsuite.monitor.server.dto.processexecution.ProcessExecution;
import org.gridsuite.monitor.server.entities.processexecution.ProcessExecutionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Radouane Khouadri <radouane.khouadri at rte-france.com>
 */
@Mapper(config = MapStructConfig.class)
public interface ProcessExecutionMapper {
    @Mapping(target = "userIdentity", ignore = true)
    ProcessExecution toDto(ProcessExecutionEntity entity);
}
