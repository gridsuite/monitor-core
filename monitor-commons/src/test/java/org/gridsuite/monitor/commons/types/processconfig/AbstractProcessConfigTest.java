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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
public abstract class AbstractProcessConfigTest<C extends ProcessConfig> {
    abstract ProcessType getProcessType();

    // number of fields to compare in compareWith()
    abstract int getFieldsNumber();

    abstract C createProcessConfig();

    abstract C createProcessConfig(List<ModificationInfo> modifications);

    @Test
    void compareWithShouldReturnNoDifferenceWhenConfigsAreEqual() {
        C processConfig = createProcessConfig();

        List<ProcessConfigFieldComparison> result = processConfig.compareWith(processConfig);

        assertThat(result)
            .hasSize(getFieldsNumber())
            .allMatch(ProcessConfigFieldComparison::identical)
            .allMatch(fieldComparison -> fieldComparison.value1().equals(fieldComparison.value2()));
    }

    @Test
    void compareWithShouldReturnDifferentModificationsWhenModificationsAreDifferent() {
        List<ModificationInfo> modifications1 = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));
        List<ModificationInfo> modifications2 = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));

        C processConfig1 = createProcessConfig(modifications1);
        C processConfig2 = createProcessConfig(modifications2);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(getFieldsNumber());
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
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        List<ModificationInfo> modifications1 = List.of(
            new ModificationInfo(mod1, "descr1", true),
            new ModificationInfo(mod2, "descr2", true));
        List<ModificationInfo> modifications2 = List.of(
            new ModificationInfo(mod2, "descr2", true),
            new ModificationInfo(mod1, "descr1", true)); // Different order

        C processConfig1 = createProcessConfig(modifications1);
        C processConfig2 = createProcessConfig(modifications2);

        List<ProcessConfigFieldComparison> result = processConfig1.compareWith(processConfig2);

        assertThat(result).hasSize(getFieldsNumber());
        ProcessConfigFieldComparison comparison = result.stream()
            .filter(d -> "modifications".equals(d.field()))
            .findFirst()
            .orElseThrow();
        assertThat(comparison.identical()).isFalse();
        assertThat(comparison.value1()).isEqualTo(modifications1);
        assertThat(comparison.value2()).isEqualTo(modifications2);
    }

    @Test
    void compareWithShouldThrowWhenProcessTypesAreDifferent() {
        C processConfig1 = createProcessConfig();
        ProcessConfig processConfig2 = getProcessType() == ProcessType.LOADFLOW
            ? new SecurityAnalysisConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)), UUID.randomUUID())
            : new LoadFlowConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)));

        assertThatThrownBy(() -> processConfig1.compareWith(processConfig2)).isInstanceOf(ClassCastException.class);
    }
}
