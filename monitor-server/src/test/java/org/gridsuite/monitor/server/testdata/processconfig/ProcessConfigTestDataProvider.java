package org.gridsuite.monitor.server.testdata.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public interface ProcessConfigTestDataProvider<D extends ProcessConfig, E extends ProcessConfigEntity> {
    // --- dto side---
    D createDto();

    D updateDto(D dto);

    // --- entity side ---
    E createEntity();

    E updateEntity(E entity);

    Class<E> entityType();

    Class<D> dtoType();

    default UUID getId(E entity) {
        return entity.getId();
    }

    default void assertEntityEquals(E expected, E actual) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    default void assertDtoEquals(D expected, D actual) {
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    default void assertDtoEntityEquivalent(D dto, E entity) {
        assertThat(entity.getProcessType()).isEqualTo(dto.processType());
        assertThat(entity.getModificationUuids())
                .isEqualTo(dto.modificationUuids());
    }
}
