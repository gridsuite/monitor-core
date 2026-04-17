/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadflowConfig;
import org.gridsuite.monitor.server.entities.processconfig.LoadflowConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Mapper(componentModel = "spring")
public interface LoadflowConfigMapper {
    @Mapping(target = "processType", expression = "java(dto.processType())")
    LoadflowConfigEntity toEntity(LoadflowConfig dto);

    LoadflowConfig toDto(LoadflowConfigEntity entity);

    void updateEntityFromDto(LoadflowConfig dto, @MappingTarget LoadflowConfigEntity entity);
}
