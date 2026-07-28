/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.mappers.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ModificationInfo;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.server.entities.processconfig.ModificationInfoEmbeddable;
import org.gridsuite.monitor.server.entities.processconfig.ShortCircuitConfigEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Caroline Jeandat <caroline.jeandat at rte-france.com>
 */
class ShortCircuitConfigMapperTest {

    private final ShortCircuitConfigMapper mapper = Mappers.getMapper(ShortCircuitConfigMapper.class);

    @Test
    void toEntity() {
        ShortCircuitConfig dto = new ShortCircuitConfig(
            UUID.randomUUID(),
            List.of(new ModificationInfo(UUID.randomUUID(), "descr1", true),
                new ModificationInfo(UUID.randomUUID(), "descr2", true),
                new ModificationInfo(UUID.randomUUID(), "descr3", true))
        );

        ShortCircuitConfigEntity entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getShortCircuitParametersUuid()).isEqualTo(dto.shortCircuitParametersUuid());
        assertThat(entity.getModifications().stream().map(ModificationInfoEmbeddable::getModificationUuid).toList())
            .isEqualTo(dto.modifications().stream().map(ModificationInfo::modificationUuid).toList());
        assertThat(entity.getModifications().stream().map(ModificationInfoEmbeddable::getDescription).toList())
            .isEqualTo(dto.modifications().stream().map(ModificationInfo::description).toList());
        assertThat(entity.getModifications().stream().map(ModificationInfoEmbeddable::isActive).toList())
            .isEqualTo(dto.modifications().stream().map(ModificationInfo::active).toList());
        assertThat(entity.getProcessType()).isEqualTo(dto.processType());
    }
}
