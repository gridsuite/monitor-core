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
class SecurityAnalysisConfigTest {
    @Test
    void compareWithShouldReturnNoDifferenceWhenConfigsAreEqual() {
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(UUID.randomUUID(),
            List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)), UUID.randomUUID());

        List<ProcessConfigFieldComparison> result = securityAnalysisConfig.compareWith(securityAnalysisConfig);

        assertThat(result)
            .hasSize(3)
            .allMatch(ProcessConfigFieldComparison::identical)
            .allMatch(fieldComparison -> fieldComparison.value1().equals(fieldComparison.value2()));
    }

    @Test
    void compareWithShouldReturnDifferentModificationsWhenModificationsAreDifferent() {
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        List<ModificationInfo> modifications1 = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));
        List<ModificationInfo> modifications2 = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));
        UUID loadflowParametersUuid = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modifications1, loadflowParametersUuid);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modifications2, loadflowParametersUuid);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(3);
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
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<ModificationInfo> modifications1 = List.of(
            new ModificationInfo(mod1, "descr1", true),
            new ModificationInfo(mod2, "descr2", true));
        List<ModificationInfo> modifications2 = List.of(
            new ModificationInfo(mod2, "descr2", true),
            new ModificationInfo(mod1, "descr1", true)); // Different order

        UUID loadflowParametersUuid = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modifications1, loadflowParametersUuid);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modifications2, loadflowParametersUuid);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(3);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modifications1);
        assertThat(comparison.value2()).isEqualTo(modifications2);
    }

    @Test
    void compareWithShouldReturnDifferencesWhenSecurityAnalysisParametersAreDifferent() {
        UUID securityAnalysisParametersUuid1 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid2 = UUID.randomUUID();
        List<ModificationInfo> modifications = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));
        UUID loadflowParametersUuid = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid1, modifications, loadflowParametersUuid);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid2, modifications, loadflowParametersUuid);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(3);
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "securityAnalysisParameters".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(securityAnalysisParametersUuid1);
        assertThat(comparison.value2()).isEqualTo(securityAnalysisParametersUuid2);
    }

    @Test
    void compareWithShouldReturnDifferencesWhenLoadflowParametersAreDifferent() {
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        List<ModificationInfo> modifications = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));
        UUID loadflowParametersUuid1 = UUID.randomUUID();
        UUID loadflowParametersUuid2 = UUID.randomUUID();

        SecurityAnalysisConfig processConfig1 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modifications, loadflowParametersUuid1);
        SecurityAnalysisConfig processConfig2 = new SecurityAnalysisConfig(securityAnalysisParametersUuid, modifications, loadflowParametersUuid2);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(3);
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
        SecurityAnalysisConfig securityAnalysisConfig = new SecurityAnalysisConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)), UUID.randomUUID());
        LoadFlowConfig loadFlowConfig = new LoadFlowConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)));

        assertThatThrownBy(() -> securityAnalysisConfig.compareWith(loadFlowConfig)).isInstanceOf(ClassCastException.class);
    }
}
