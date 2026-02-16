/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mapper;

import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.SecurityAnalysisConfigEntity;

import java.time.Instant;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class SecurityAnalysisConfigMapper {
    public static SecurityAnalysisConfigEntity toEntity(SecurityAnalysisConfig dto, String owner) {
        SecurityAnalysisConfigEntity entity = new SecurityAnalysisConfigEntity();
        entity.setOwner(owner);
        entity.setLastModifiedBy(owner);
        Instant now = Instant.now();
        entity.setCreationDate(now);

        entity.setLastModificationDate(now);
        entity.setParametersUuid(dto.getParametersUuid());
        entity.setContingencies(dto.getContingencies());
        entity.setModificationUuids(dto.getModificationUuids());
        return entity;
    }

    public static SecurityAnalysisConfig toDto(SecurityAnalysisConfigEntity entity) {
        return new SecurityAnalysisConfig(
            entity.getParametersUuid(),
            entity.getContingencies(),
            entity.getModificationUuids(),
            entity.getOwner(),
            entity.getCreationDate(),
            entity.getLastModificationDate(),
            entity.getLastModifiedBy()
        );
    }

    public static void update(SecurityAnalysisConfigEntity entity, SecurityAnalysisConfig dto, String userId) {
        entity.setLastModifiedBy(userId);
        entity.setLastModificationDate(Instant.now());

        entity.setParametersUuid(dto.getParametersUuid());
        entity.setContingencies(dto.getContingencies());
        entity.setModificationUuids(dto.getModificationUuids());
    }
}
