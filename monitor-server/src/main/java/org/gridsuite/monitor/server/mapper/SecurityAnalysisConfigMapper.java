/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class SecurityAnalysisConfigMapper {
    public static SecurityAnalysisConfigEntity toEntity(SecurityAnalysisConfig dto) {
        SecurityAnalysisConfigEntity entity = new SecurityAnalysisConfigEntity();
        update(entity, dto);
        return entity;
    }

    public static SecurityAnalysisConfig toDto(SecurityAnalysisConfigEntity entity) {
        return new SecurityAnalysisConfig(
            entity.getParametersUuid(),
            entity.getContingencies(),
            entity.getModificationUuids()
        );
    }

    public static void update(SecurityAnalysisConfigEntity entity, SecurityAnalysisConfig dto) {
        entity.setParametersUuid(dto.parametersUuid());
        entity.setContingencies(dto.contingencies());
        entity.setModificationUuids(dto.modificationUuids());
    }
}
