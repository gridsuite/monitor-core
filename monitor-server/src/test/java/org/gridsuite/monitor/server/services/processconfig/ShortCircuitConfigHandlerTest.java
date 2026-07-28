/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ModificationInfo;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.ShortCircuitConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.ShortCircuitConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
class ShortCircuitConfigHandlerTest extends AbstractProcessConfigHandlerTest<ShortCircuitConfig, ShortCircuitConfigEntity, ShortCircuitConfigMapper, ShortCircuitConfigHandler> {

    @Override
    @BeforeEach
    protected void setUp() {
        ShortCircuitConfigMapper realMapper = Mappers.getMapper(ShortCircuitConfigMapper.class);
        mapper = spy(realMapper);
        handler = new ShortCircuitConfigHandler(mapper);
    }

    @Override
    ProcessType getProcessType() {
        return ProcessType.SHORT_CIRCUIT;
    }

    @Override
    ShortCircuitConfig createProcessConfig() {
        return new ShortCircuitConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)));
    }

    @Override
    ShortCircuitConfigEntity createProcessConfigEntity() {
        return new ShortCircuitConfigEntity();
    }
}
