/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.PersistedProcessConfig;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author Radouane Khouadri <radouane.khouadri at rte-france.com>
 */
@Mapper(componentModel = "spring")
public interface SecurityAnalysisConfigMapper {

    @Mapping(target = "processType", expression = "java(dto.processType())")
    SecurityAnalysisConfigEntity toEntity(SecurityAnalysisConfig dto);

    SecurityAnalysisConfig toDto(SecurityAnalysisConfigEntity dto);

    @Mapping(target = "processConfig", source = ".")
    PersistedProcessConfig toPersistedProcessConfigDto(SecurityAnalysisConfigEntity entity);

    void updateEntityFromDto(SecurityAnalysisConfig dto, @MappingTarget SecurityAnalysisConfigEntity entity);

}
