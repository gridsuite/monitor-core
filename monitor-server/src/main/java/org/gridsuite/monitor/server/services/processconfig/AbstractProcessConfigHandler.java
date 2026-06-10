/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.ProcessConfigMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
public abstract class AbstractProcessConfigHandler<C extends ProcessConfig, E extends ProcessConfigEntity, M extends ProcessConfigMapper<C, E>>
    implements ProcessConfigHandler<C, E> {

    protected final M mapper;

    protected AbstractProcessConfigHandler(M mapper) {
        this.mapper = mapper;
    }

    @Override
    public void update(E entity, C processConfig) {
        mapper.updateEntityFromDto(processConfig, entity);
    }

    @Override
    public E copyEntity(E sourceEntity) {
        return toEntity(toProcessConfig(sourceEntity));
    }

    @Override
    public E toEntity(C processConfig) {
        return mapper.toEntity(processConfig);
    }

    @Override
    public C toProcessConfig(E entity) {
        return mapper.toDto(entity);
    }

    @Override
    public List<ProcessConfigFieldComparison> computeDifferences(E entity1, E entity2) {
        C config1 = toProcessConfig(entity1);
        C config2 = toProcessConfig(entity2);
        List<ProcessConfigFieldComparison> differences = new ArrayList<>();

        // Compare modifications
        addFieldComparison(config1.modificationUuids(), config2.modificationUuids(), differences, "modifications");

        // Compare other fields
        addProcessConfigSpecificFieldsComparison(config1, config2, differences);

        return differences;
    }

    protected abstract void addProcessConfigSpecificFieldsComparison(C config1, C config2, List<ProcessConfigFieldComparison> differences);

    protected void addFieldComparison(Object value1, Object value2, List<ProcessConfigFieldComparison> differences, String fieldName) {
        differences.add(new ProcessConfigFieldComparison(
            fieldName,
            Objects.equals(value1, value2),
            value1,
            value2
        ));
    }
}
