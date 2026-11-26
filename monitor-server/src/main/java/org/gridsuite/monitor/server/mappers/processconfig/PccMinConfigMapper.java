/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.PccMinConfig;
import org.gridsuite.monitor.server.config.MapStructConfig;
import org.gridsuite.monitor.server.entities.processconfig.PccMinConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Mapper(config = MapStructConfig.class)
public interface PccMinConfigMapper extends ProcessConfigMapper<PccMinConfig, PccMinConfigEntity> {
    @Mapping(target = "id", ignore = true)
    PccMinConfigEntity toEntity(PccMinConfig dto);

    PccMinConfig toDto(PccMinConfigEntity entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(PccMinConfig dto, @MappingTarget PccMinConfigEntity entity);
}
