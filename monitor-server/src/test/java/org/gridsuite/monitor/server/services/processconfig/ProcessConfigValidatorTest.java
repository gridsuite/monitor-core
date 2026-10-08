/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processconfig.ShortCircuitConfig;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.services.processconfig.validators.ProcessConfigValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROVIDER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ProcessConfigValidatorTest {
    private ProcessConfigValidator processConfigValidator;

    @Mock
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    @Mock
    private LoadflowRestClient loadflowRestClient;

    @BeforeEach
    void setUp() {
        processConfigValidator = new ProcessConfigValidator(
            loadflowRestClient,
            securityAnalysisRestClient
        );
    }

    @Test
    void validatePassingLoadFlowProcessConfig() {
        UUID loadflowParamUuid = UUID.randomUUID();
        LoadFlowConfig loadFlowConfig = new LoadFlowConfig(loadflowParamUuid, List.of());

        doReturn("OpenLoadFlow").when(loadflowRestClient).getParameterProvider(loadflowParamUuid);

        assertDoesNotThrow(() -> processConfigValidator.validate(loadFlowConfig));

        verify(loadflowRestClient).getParameterProvider(loadflowParamUuid);
    }

    @Test
    void validateNotPassingLoadFlowProcessConfig() {
        UUID loadflowParamUuid = UUID.randomUUID();
        LoadFlowConfig loadFlowConfig = new LoadFlowConfig(loadflowParamUuid, List.of());

        doReturn("OtherProvider").when(loadflowRestClient).getParameterProvider(loadflowParamUuid);

        MonitorServerException exception = assertThrows(MonitorServerException.class, () -> processConfigValidator.validate(loadFlowConfig));

        assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_PROVIDER);
        verify(loadflowRestClient).getParameterProvider(loadflowParamUuid);
    }

    @Test
    void validatePassingSAProcessConfig() {
        UUID loadflowParamUuid = UUID.randomUUID();
        UUID saParamUuid = UUID.randomUUID();
        SecurityAnalysisConfig saConfig = new SecurityAnalysisConfig(saParamUuid, List.of(), loadflowParamUuid);

        doReturn("OpenLoadFlow").when(securityAnalysisRestClient).getParameterProvider(saParamUuid);
        doReturn("OpenLoadFlow").when(loadflowRestClient).getParameterProvider(loadflowParamUuid);

        assertDoesNotThrow(() -> processConfigValidator.validate(saConfig));

        verify(loadflowRestClient).getParameterProvider(loadflowParamUuid);
        verify(securityAnalysisRestClient).getParameterProvider(saParamUuid);
    }

    @Test
    void validateNotPassingSAProcessConfig() {
        UUID loadflowParamUuid = UUID.randomUUID();
        UUID saParamUuid = UUID.randomUUID();
        SecurityAnalysisConfig saConfig = new SecurityAnalysisConfig(saParamUuid, List.of(), loadflowParamUuid);

        doReturn("OtherProvider").when(securityAnalysisRestClient).getParameterProvider(saParamUuid);
        doReturn("OpenLoadFlow").when(loadflowRestClient).getParameterProvider(loadflowParamUuid);

        MonitorServerException exception = assertThrows(MonitorServerException.class, () -> processConfigValidator.validate(saConfig));

        assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_PROVIDER);
        verify(loadflowRestClient).getParameterProvider(loadflowParamUuid);
        verify(securityAnalysisRestClient).getParameterProvider(saParamUuid);
    }

    @Test
    void validateSCProcessConfig() {
        assertDoesNotThrow(() -> processConfigValidator.validate(new ShortCircuitConfig(UUID.randomUUID(), List.of())));
    }
}
