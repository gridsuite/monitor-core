/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NewSecurityAnalysisConfigMapper {
    // Instance of the mapper
    NewSecurityAnalysisConfigMapper INSTANCE = Mappers.getMapper(NewSecurityAnalysisConfigMapper.class);

    // Map Entity to DTO
    SecurityAnalysisConfig entityToDto(SecurityAnalysisConfigEntity entity);

    // Map DTO to Entity
    @Mapping(target = "processType", expression = "java(dto.processType())")
    SecurityAnalysisConfigEntity dtoToEntity(SecurityAnalysisConfig dto);
}
