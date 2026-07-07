/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.commons.types.processconfig;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
class LoadFlowConfigTest {
    @Test
    void compareWithShouldReturnNoDifferenceWhenConfigsAreEqual() {
        LoadFlowConfig processConfig = new LoadFlowConfig(UUID.randomUUID(), List.of(UUID.randomUUID()));

        List<ProcessConfigFieldComparison> result = processConfig.compareWith(processConfig);

        assertThat(result)
            .hasSize(2)
            .allMatch(ProcessConfigFieldComparison::identical)
            .allMatch(fieldComparison -> fieldComparison.value1().equals(fieldComparison.value2()));
    }

    @Test
    void compareWithShouldReturnDifferentModificationsWhenModificationsAreDifferent() {
        UUID loadflowParametersUuid = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> modificationUuids2 = List.of(UUID.randomUUID(), UUID.randomUUID());

        LoadFlowConfig processConfig1 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids1);
        LoadFlowConfig processConfig2 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids2);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(2);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modificationUuids1);
        assertThat(comparison.value2()).isEqualTo(modificationUuids2);
    }

    @Test
    void compareWithShouldDetectOrderDifferenceInModifications() {
        UUID loadflowParametersUuid = UUID.randomUUID();
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<UUID> modificationUuids1 = List.of(mod1, mod2);
        List<UUID> modificationUuids2 = List.of(mod2, mod1); // Different order

        LoadFlowConfig processConfig1 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids1);
        LoadFlowConfig processConfig2 = new LoadFlowConfig(loadflowParametersUuid, modificationUuids2);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(2);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modificationUuids1);
        assertThat(comparison.value2()).isEqualTo(modificationUuids2);
    }

    @Test
    void compareWithShouldReturnDifferencesWhenParametersAreDifferent() {
        UUID loadflowParametersUuid1 = UUID.randomUUID();
        UUID loadflowParametersUuid2 = UUID.randomUUID();
        List<UUID> modificationUuids = List.of(UUID.randomUUID(), UUID.randomUUID());

        LoadFlowConfig processConfig1 = new LoadFlowConfig(loadflowParametersUuid1, modificationUuids);
        LoadFlowConfig processConfig2 = new LoadFlowConfig(loadflowParametersUuid2, modificationUuids);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(2);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "loadflowParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(loadflowParametersUuid1);
        assertThat(comparison.value2()).isEqualTo(loadflowParametersUuid2);
    }

    @Test
    void compareWithShouldThrowWhenProcessTypesAreDifferent() {
        LoadFlowConfig loadFlowConfig = new LoadFlowConfig(UUID.randomUUID(), List.of(UUID.randomUUID()));
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(UUID.randomUUID(), List.of(UUID.randomUUID()), UUID.randomUUID());

        assertThatThrownBy(() -> loadFlowConfig.compareWith(securityAnalysisConfig)).isInstanceOf(ClassCastException.class);
    }
}
