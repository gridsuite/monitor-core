package org.gridsuite.monitor.server.testdata.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;

import static org.assertj.core.api.Assertions.assertThat;

public interface ProcessConfigTestDataProvider<D extends ProcessConfig, E extends ProcessConfigEntity> {-

    D createDto();

    E createEntity();

    Class<E> entityType();

    Class<D> dtoType();

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
