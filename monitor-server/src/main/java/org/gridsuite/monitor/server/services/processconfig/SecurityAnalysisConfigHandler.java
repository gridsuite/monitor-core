/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.server.entities.processconfig.SecurityAnalysisConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
@Service
public class SecurityAnalysisConfigHandler extends AbstractProcessConfigHandler<SecurityAnalysisConfig, SecurityAnalysisConfigEntity, SecurityAnalysisConfigMapper> {

    public SecurityAnalysisConfigHandler(SecurityAnalysisConfigMapper mapper) {
        super(mapper);
    }

    @Override
    public ProcessType getProcessType() {
        return ProcessType.SECURITY_ANALYSIS;
    }

    @Override
    public List<ProcessConfigFieldComparison> computeDifferences(SecurityAnalysisConfigEntity entity1, SecurityAnalysisConfigEntity entity2) {
        // error si pas bon processType
        SecurityAnalysisConfig config1 = toProcessConfig(entity1);
        SecurityAnalysisConfig config2 = toProcessConfig(entity2);
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

        // Compare security analysis parameters
        differences.add(new ProcessConfigFieldComparison(
            "securityAnalysisParameters",
            Objects.equals(config1.securityAnalysisParametersUuid(), config2.securityAnalysisParametersUuid()),
            config1.securityAnalysisParametersUuid(),
            config2.securityAnalysisParametersUuid()
        ));

        return differences;
    }
}
