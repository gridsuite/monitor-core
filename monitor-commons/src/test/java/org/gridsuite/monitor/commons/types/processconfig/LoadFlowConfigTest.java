/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
class LoadFlowConfigTest extends AbstractProcessConfigTest<LoadFlowConfig> {

    @Override
    ProcessType getExpectedProcessType() {
        return ProcessType.LOADFLOW;
    }

    @Override
    LoadFlowConfig createProcessConfig(List<ModificationInfo> modifications) {
        return new LoadFlowConfig(UUID.randomUUID(), modifications);
    }

    @Test
    void compareWithShouldReturnDifferencesWhenParametersAreDifferent() {
        UUID loadflowParametersUuid1 = UUID.randomUUID();
        UUID loadflowParametersUuid2 = UUID.randomUUID();
        List<ModificationInfo> modifications = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));

        LoadFlowConfig processConfig1 = new LoadFlowConfig(loadflowParametersUuid1, modifications);
        LoadFlowConfig processConfig2 = new LoadFlowConfig(loadflowParametersUuid2, modifications);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "loadflowParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(loadflowParametersUuid1);
        assertThat(comparison.value2()).isEqualTo(loadflowParametersUuid2);
    }
}
