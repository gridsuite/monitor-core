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
class ShortCircuitConfigTest extends AbstractProcessConfigTest<ShortCircuitConfig> {

    UUID shortCircuitParametersUuid = UUID.randomUUID();

    @Override
    ProcessType getProcessType() {
        return ProcessType.SHORT_CIRCUIT;
    }

    @Override
    int getFieldsNumber() {
        return 2;
    }

    @Override
    ShortCircuitConfig createProcessConfig() {
        return new ShortCircuitConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)));
    }

    @Override
    ShortCircuitConfig createProcessConfig(List<ModificationInfo> modifications) {
        return new ShortCircuitConfig(shortCircuitParametersUuid, modifications);
    }

    @Test
    void compareWithShouldReturnDifferencesWhenParametersAreDifferent() {
        UUID shortCircuitParametersUuid1 = UUID.randomUUID();
        UUID shortCircuitParametersUuid2 = UUID.randomUUID();
        List<ModificationInfo> modifications = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));

        ShortCircuitConfig processConfig1 = new ShortCircuitConfig(shortCircuitParametersUuid1, modifications);
        ShortCircuitConfig processConfig2 = new ShortCircuitConfig(shortCircuitParametersUuid2, modifications);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(getFieldsNumber());
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "shortCircuitParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(shortCircuitParametersUuid1);
        assertThat(comparison.value2()).isEqualTo(shortCircuitParametersUuid2);
    }
}
