/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.server.dto.ProcessExecution;
import org.gridsuite.monitor.server.entities.ProcessExecutionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NewProcessExecutionMapper {
    // Instance of the mapper
    NewProcessExecutionMapper INSTANCE = Mappers.getMapper(NewProcessExecutionMapper.class);

    // Map Entity to DTO
    ProcessExecution entityToDto(ProcessExecutionEntity entity);

    // Map DTO to Entity
    ProcessExecutionEntity dtoToEntity(ProcessExecution dto);
}
