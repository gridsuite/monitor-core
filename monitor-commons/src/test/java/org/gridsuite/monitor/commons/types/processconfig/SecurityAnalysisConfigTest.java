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
class SecurityAnalysisConfigTest extends AbstractProcessConfigTest<SecurityAnalysisConfig> {

    UUID securityAnalysisParametersUuid = UUID.randomUUID();
    UUID loadflowParametersUuid = UUID.randomUUID();

    @Override
    ProcessType getProcessType() {
        return ProcessType.SECURITY_ANALYSIS;
    }

    @Override
    int getFieldsNumber() {
        return 3;
    }

    @Override
    SecurityAnalysisConfig createProcessConfig() {
        return new SecurityAnalysisConfig(UUID.randomUUID(), List.of(new ModificationInfo(UUID.randomUUID(), "descr", true)), UUID.randomUUID());
    }

    @Override
    SecurityAnalysisConfig createProcessConfig(List<ModificationInfo> modifications) {
        return new SecurityAnalysisConfig(securityAnalysisParametersUuid, modifications, loadflowParametersUuid);
    }

    @Test
    void compareWithShouldReturnDifferencesWhenSecurityAnalysisParametersAreDifferent() {
        UUID securityAnalysisParametersUuid1 = UUID.randomUUID();
        UUID securityAnalysisParametersUuid2 = UUID.randomUUID();
        List<ModificationInfo> modifications = List.of(
            new ModificationInfo(UUID.randomUUID(), "descr1", true),
            new ModificationInfo(UUID.randomUUID(), "descr2", true));

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
}
