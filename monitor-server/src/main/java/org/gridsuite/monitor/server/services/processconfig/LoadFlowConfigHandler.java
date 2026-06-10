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

import java.util.List;

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
    protected void addProcessConfigSpecificFieldsComparison(LoadFlowConfig config1, LoadFlowConfig config2, List<ProcessConfigFieldComparison> differences) {
        // Compare loadflow parameters
        addFieldComparison(config1.loadflowParametersUuid(), config2.loadflowParametersUuid(), differences, "loadflowParameters");
    }
}
