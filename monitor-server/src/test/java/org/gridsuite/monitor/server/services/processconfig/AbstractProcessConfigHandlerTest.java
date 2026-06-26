/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.ProcessConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
public abstract class AbstractProcessConfigHandlerTest<
    C extends ProcessConfig,
    E extends ProcessConfigEntity,
    M extends ProcessConfigMapper<C, E>,
    H extends ProcessConfigHandler<C, E>> {

    protected M mapper;

    protected H handler;

    abstract ProcessType getProcessType();

    abstract C createProcessConfig();

    abstract E createProcessConfigEntity();

    @BeforeEach
    protected abstract void setUp();

    @Test
    void getProcessTypeTest() {
        assertThat(handler.getProcessType()).isEqualTo(getProcessType());
    }

    @Test
    void updateTest() {
        E processConfigEntity = createProcessConfigEntity();
        C processConfig = createProcessConfig();

        handler.update(processConfig, processConfigEntity);

        verify(mapper).updateEntityFromDto(processConfig, processConfigEntity);
    }

    @Test
    void copyEntityTest() {
        E processConfigEntity1 = createProcessConfigEntity();
        E expectedProcessConfigEntity = createProcessConfigEntity();
        C processConfig = createProcessConfig();

        when(mapper.toDto(processConfigEntity1)).thenReturn(processConfig);
        when(mapper.toEntity(processConfig)).thenReturn(expectedProcessConfigEntity);

        ProcessConfigEntity result = handler.copyEntity(processConfigEntity1);

        assertThat(result).isEqualTo(expectedProcessConfigEntity);
        verify(mapper).toDto(processConfigEntity1);
        verify(mapper).toEntity(any());
    }

    @Test
    void toEntityTest() {
        E expectedProcessConfigEntity = createProcessConfigEntity();
        C processConfig = createProcessConfig();

        when(mapper.toEntity(processConfig)).thenReturn(expectedProcessConfigEntity);

        ProcessConfigEntity result = handler.toEntity(processConfig);

        assertThat(result).isEqualTo(expectedProcessConfigEntity);
        verify(mapper).toEntity(processConfig);
    }

    @Test
    void toDtoTest() {
        E processConfigEntity = createProcessConfigEntity();
        C expectedProcessConfig = createProcessConfig();

        when(mapper.toDto(processConfigEntity)).thenReturn(expectedProcessConfig);

        ProcessConfig result = handler.toDto(processConfigEntity);

        assertThat(result).isEqualTo(expectedProcessConfig);
        verify(mapper).toDto(processConfigEntity);
    }
}
