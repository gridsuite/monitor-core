/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROVIDER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAnalysisConfigValidatorTest {
    @Mock
    private LoadflowRestClient loadflowRestClient;
    @Mock
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    private SecurityAnalysisConfigValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SecurityAnalysisConfigValidator(loadflowRestClient, securityAnalysisRestClient);
    }

    @Test
    void getProcessTypeTest() {
        assertThat(validator.getProcessType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);
    }

    @Test
    void validateWithSupportedProviders() {
        UUID loadflowParametersUuid = UUID.randomUUID();
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        SecurityAnalysisConfig config = new SecurityAnalysisConfig(securityAnalysisParametersUuid, List.of(), loadflowParametersUuid);
        when(loadflowRestClient.getParameterProvider(loadflowParametersUuid)).thenReturn("OpenLoadFlow");
        when(securityAnalysisRestClient.getParameterProvider(securityAnalysisParametersUuid)).thenReturn("OpenLoadFlow");

        assertDoesNotThrow(() -> validator.validate(config));

        verify(loadflowRestClient).getParameterProvider(loadflowParametersUuid);
        verify(securityAnalysisRestClient).getParameterProvider(securityAnalysisParametersUuid);
    }

    @ParameterizedTest
    @CsvSource({
        "OtherProvider, OpenLoadFlow",
        "OpenLoadFlow, OtherProvider",
        "OtherProvider, OtherProvider"
    })
    void validateWithUnsupportedProviders(String loadflowProvider, String securityAnalysisProvider) {
        UUID loadflowParametersUuid = UUID.randomUUID();
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        SecurityAnalysisConfig config = new SecurityAnalysisConfig(securityAnalysisParametersUuid, List.of(), loadflowParametersUuid);
        when(loadflowRestClient.getParameterProvider(loadflowParametersUuid)).thenReturn(loadflowProvider);
        when(securityAnalysisRestClient.getParameterProvider(securityAnalysisParametersUuid)).thenReturn(securityAnalysisProvider);

        MonitorServerException exception = assertThrows(MonitorServerException.class, () -> validator.validate(config));

        assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_PROVIDER);
        verify(loadflowRestClient).getParameterProvider(loadflowParametersUuid);
        verify(securityAnalysisRestClient).getParameterProvider(securityAnalysisParametersUuid);
    }

    @Test
    void validateWithLoadflowClientFailure() {
        UUID loadflowParametersUuid = UUID.randomUUID();
        SecurityAnalysisConfig config = new SecurityAnalysisConfig(UUID.randomUUID(), List.of(), loadflowParametersUuid);
        IllegalStateException exception = new IllegalStateException("Loadflow service unavailable");
        when(loadflowRestClient.getParameterProvider(loadflowParametersUuid)).thenThrow(exception);

        assertThat(assertThrows(IllegalStateException.class, () -> validator.validate(config))).isSameAs(exception);
        verifyNoInteractions(securityAnalysisRestClient);
    }

    @Test
    void validateWithSecurityAnalysisClientFailure() {
        UUID loadflowParametersUuid = UUID.randomUUID();
        UUID securityAnalysisParametersUuid = UUID.randomUUID();
        SecurityAnalysisConfig config = new SecurityAnalysisConfig(securityAnalysisParametersUuid, List.of(), loadflowParametersUuid);
        IllegalStateException exception = new IllegalStateException("Security analysis service unavailable");
        when(loadflowRestClient.getParameterProvider(loadflowParametersUuid)).thenReturn("OpenLoadFlow");
        when(securityAnalysisRestClient.getParameterProvider(securityAnalysisParametersUuid)).thenThrow(exception);

        assertThat(assertThrows(IllegalStateException.class, () -> validator.validate(config))).isSameAs(exception);
    }
}
