/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processconfig;

import org.gridsuite.monitor.commons.api.types.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.commons.api.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.api.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class SecurityAnalysisConfigMapper {
    public static SecurityAnalysisConfigEntity toEntity(SecurityAnalysisConfig dto) {
        SecurityAnalysisConfigEntity entity = new SecurityAnalysisConfigEntity();
        entity.setProcessType(ProcessType.SECURITY_ANALYSIS);
        update(entity, dto);
        return entity;
    }

    public static PersistedProcessConfig toDto(SecurityAnalysisConfigEntity entity) {
        return new PersistedProcessConfig(entity.getId(), new SecurityAnalysisConfig(
            entity.getParametersUuid(),
            entity.getContingencies(),
            entity.getModificationUuids()
        ));
    }

    public static void update(SecurityAnalysisConfigEntity entity, SecurityAnalysisConfig dto) {
        entity.setParametersUuid(dto.parametersUuid());
        entity.setContingencies(dto.contingencies());
        entity.setModificationUuids(dto.modificationUuids());
    }
}
