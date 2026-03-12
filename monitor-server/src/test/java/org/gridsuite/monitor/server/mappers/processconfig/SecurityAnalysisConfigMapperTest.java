/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Radouane Khouadri <radouane.khouadri at rte-france.com>
 */
class SecurityAnalysisConfigMapperTest {

    private final SecurityAnalysisConfigMapper mapper = Mappers.getMapper(SecurityAnalysisConfigMapper.class);

    @Test
    void toEntity() {
        SecurityAnalysisConfig dto = new SecurityAnalysisConfig(
                UUID.randomUUID(),
                List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                UUID.randomUUID()
        );

        SecurityAnalysisConfigEntity entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getSecurityAnalysisParametersUuid()).isEqualTo(dto.securityAnalysisParametersUuid());
        assertThat(entity.getLoadflowParametersUuid()).isEqualTo(dto.loadflowParametersUuid());
        assertThat(entity.getModificationUuids()).isEqualTo(dto.modificationUuids());
        assertThat(entity.getProcessType()).isEqualTo(dto.processType());
    }
}
