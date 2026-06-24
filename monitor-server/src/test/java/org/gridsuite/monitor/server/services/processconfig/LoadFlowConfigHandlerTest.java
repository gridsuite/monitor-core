/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
class LoadFlowConfigHandlerTest extends AbstractProcessConfigHandlerTest<LoadFlowConfig, LoadFlowConfigEntity, LoadFlowConfigMapper, LoadFlowConfigHandler> {
    @Override
    ProcessType getProcessType() {
        return ProcessType.LOADFLOW;
    }

    @Override
    LoadFlowConfig createProcessConfig() {
        return new LoadFlowConfig(UUID.randomUUID(), List.of(UUID.randomUUID()));
    }

    @Override
    LoadFlowConfigEntity createProcessConfigEntity() {
        return new LoadFlowConfigEntity();
    }

    @Override
    @BeforeEach
    protected void setUp() {
        LoadFlowConfigMapper realMapper = Mappers.getMapper(LoadFlowConfigMapper.class);
        mapper = Mockito.spy(realMapper);
        handler = new LoadFlowConfigHandler(mapper);
    }
}
