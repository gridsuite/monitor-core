/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.mappers.processconfig.ShortCircuitConfigMapper;
import org.gridsuite.monitor.server.repositories.processconfig.LoadFlowConfigRepository;
import org.gridsuite.monitor.server.repositories.processconfig.SecurityAnalysisConfigRepository;
import org.gridsuite.monitor.server.repositories.processconfig.ShortCircuitConfigRepository;
import org.gridsuite.monitor.server.services.processconfig.providers.LoadFlowConfigProvider;
import org.gridsuite.monitor.server.services.processconfig.providers.SecurityAnalysisConfigProvider;
import org.gridsuite.monitor.server.services.processconfig.providers.ShortCircuitConfigProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProcessConfigProviderTest {

    @Test
    void loadFlowProvider() {
        LoadFlowConfigMapper mapper = mock(LoadFlowConfigMapper.class);
        LoadFlowConfigRepository repository = mock(LoadFlowConfigRepository.class);

        LoadFlowConfigProvider provider = new LoadFlowConfigProvider(mapper, repository);

        assertThat(provider.getProcessType()).isEqualTo(ProcessType.LOADFLOW);
        assertThat(provider.getMapper()).isSameAs(mapper);
        assertThat(provider.getRepository()).isSameAs(repository);
    }

    @Test
    void securityAnalysisProvider() {
        SecurityAnalysisConfigMapper mapper = mock(SecurityAnalysisConfigMapper.class);
        SecurityAnalysisConfigRepository repository = mock(SecurityAnalysisConfigRepository.class);

        SecurityAnalysisConfigProvider provider = new SecurityAnalysisConfigProvider(mapper, repository);

        assertThat(provider.getProcessType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
        assertThat(provider.getMapper()).isSameAs(mapper);
        assertThat(provider.getRepository()).isSameAs(repository);
    }

    @Test
    void shortCircuitProvider() {
        ShortCircuitConfigMapper mapper = mock(ShortCircuitConfigMapper.class);
        ShortCircuitConfigRepository repository = mock(ShortCircuitConfigRepository.class);

        ShortCircuitConfigProvider provider = new ShortCircuitConfigProvider(mapper, repository);

        assertThat(provider.getProcessType()).isEqualTo(ProcessType.SHORT_CIRCUIT);
        assertThat(provider.getMapper()).isSameAs(mapper);
        assertThat(provider.getRepository()).isSameAs(repository);
    }
}
