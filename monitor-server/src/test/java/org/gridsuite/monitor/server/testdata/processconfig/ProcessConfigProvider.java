package org.gridsuite.monitor.server.testdata.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;

import java.util.UUID;

public interface ProcessConfigProvider<D extends ProcessConfig, E extends ProcessConfigEntity> {
    // --- dto side---
    D createDto();

    D updateDto(D dto);

    // --- entity side ---
    E createEntity();

    E updateEntity(E entity);

    Class<E> entityType();

    UUID getId(E entity);

    void assertEntityEquals(E expected, E actual);

    void assertDtoEquals(D expected, D actual);

    void assertDtoEntityEquivalent(D dto, E entity);
}
