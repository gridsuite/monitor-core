/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.LoadFlowConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Service
public class LoadFlowConfigHandler extends AbstractProcessConfigHandler<LoadFlowConfig, LoadFlowConfigEntity, LoadFlowConfigMapper> {

    public LoadFlowConfigHandler(LoadFlowConfigMapper mapper) {
        super(mapper);
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.LOADFLOW;
    }

    @Override
    public List<ProcessConfigFieldComparison> computeDifferences(LoadFlowConfigEntity entity1, LoadFlowConfigEntity entity2) {
        LoadFlowConfig config1 = toProcessConfig(entity1);
        LoadFlowConfig config2 = toProcessConfig(entity2);
        List<ProcessConfigFieldComparison> differences = new ArrayList<>();

        // Compare modifications
        differences.add(new ProcessConfigFieldComparison(
            "modifications",
            Objects.equals(config1.modificationUuids(), config2.modificationUuids()),
            config1.modificationUuids(),
            config2.modificationUuids()
        ));

        // Compare loadflow parameters
        differences.add(new ProcessConfigFieldComparison(
            "loadflowParameters",
            Objects.equals(config1.loadflowParametersUuid(), config2.loadflowParametersUuid()),
            config1.loadflowParametersUuid(),
            config2.loadflowParametersUuid()
        ));

        return differences;
    }
}
