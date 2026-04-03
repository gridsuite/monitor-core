/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author Radouane Khouadri <radouane.khouadri at rte-france.com>
 */
@Mapper(componentModel = "spring")
public interface SecurityAnalysisConfigMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processType", ignore = true)
    SecurityAnalysisConfigEntity toEntity(SecurityAnalysisConfig dto);

    SecurityAnalysisConfig toDto(SecurityAnalysisConfigEntity dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processType", ignore = true)
    void updateEntityFromDto(SecurityAnalysisConfig dto, @MappingTarget SecurityAnalysisConfigEntity entity);
}
