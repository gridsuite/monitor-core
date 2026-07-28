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
class ShortCircuitConfigTest {
    @Test
    void compareWithShouldReturnNoDifferenceWhenConfigsAreEqual() {
        ShortCircuitConfig processConfig = new ShortCircuitConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)));

        List<ProcessConfigFieldComparison> result = processConfig.compareWith(processConfig);

        assertThat(result)
            .hasSize(2)
            .allMatch(ProcessConfigFieldComparison::identical)
            .allMatch(fieldComparison -> fieldComparison.value1().equals(fieldComparison.value2()));
    }

    @Test
    void compareWithShouldReturnDifferentModificationsWhenModificationsAreDifferent() {
        UUID shortCircuitParametersUuid = UUID.randomUUID();
        List<ModificationInfo> modifications1 = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));
        List<ModificationInfo> modifications2 = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));

        ShortCircuitConfig processConfig1 = new ShortCircuitConfig(shortCircuitParametersUuid, modifications1);
        ShortCircuitConfig processConfig2 = new ShortCircuitConfig(shortCircuitParametersUuid, modifications2);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(2);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modifications1);
        assertThat(comparison.value2()).isEqualTo(modifications2);
    }

    @Test
    void compareWithShouldDetectOrderDifferenceInModifications() {
        UUID shortCircuitParametersUuid = UUID.randomUUID();
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<ModificationInfo> modifications1 = List.of(
            new ModificationInfo(mod1, "descr1", true),
            new ModificationInfo(mod2, "descr2", true));
        List<ModificationInfo> modifications2 = List.of(
            new ModificationInfo(mod2, "descr2", true),
            new ModificationInfo(mod1, "descr1", true)); // Different order

        ShortCircuitConfig processConfig1 = new ShortCircuitConfig(shortCircuitParametersUuid, modifications1);
        ShortCircuitConfig processConfig2 = new ShortCircuitConfig(shortCircuitParametersUuid, modifications2);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(2);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modifications1);
        assertThat(comparison.value2()).isEqualTo(modifications2);
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

        assertThat(result).hasSize(2);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "shortCircuitParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(shortCircuitParametersUuid1);
        assertThat(comparison.value2()).isEqualTo(shortCircuitParametersUuid2);
    }

    @Test
    void compareWithShouldThrowWhenProcessTypesAreDifferent() {
        ShortCircuitConfig shortCircuitConfig = new ShortCircuitConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)));
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)), UUID.randomUUID());

        assertThatThrownBy(() -> shortCircuitConfig.compareWith(securityAnalysisConfig)).isInstanceOf(ClassCastException.class);
    }
}
