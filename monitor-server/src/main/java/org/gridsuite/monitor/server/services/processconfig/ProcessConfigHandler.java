/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.AbstractProcessConfigEntity;

import java.util.List;

/**
 * Handler for a specific {@link ProcessConfig} type. Encapsulates all type-specific
 * logic (mapping, persistence, comparison) so that {@link ProcessConfigService} remains
 * free of per-type switch statements. Add a new Spring {@code @Component} implementation
 * of this interface for every new config type.
 *
 * @param <C> the DTO type (must implement {@link ProcessConfig})
 * @param <E> the JPA entity type (must extend {@link AbstractProcessConfigEntity})
 */
public interface ProcessConfigHandler<C extends ProcessConfig, E extends AbstractProcessConfigEntity> {

    /** Returns the {@link ProcessType} this handler is responsible for. */
    ProcessType getProcessType();

    /** Converts a DTO to a new (unsaved) entity. The entity {@code id} must be left null. */
    E toEntity(C config);

    /** Converts an entity to its DTO representation. */
    C toDto(E entity);

    /**
     * Applies the fields of {@code config} onto {@code entity} in place.
     * The entity {@code id} must not be modified.
     */
    void updateEntity(C config, E entity);

    /** Returns all persisted entities of this type. */
    List<E> findAll();

    /**
     * Compares two DTOs of this type field by field.
     *
     * @return a list of {@link ProcessConfigFieldComparison} entries, one per compared field
     */
    List<ProcessConfigFieldComparison> compare(C config1, C config2);
}
