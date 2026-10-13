/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig.validators;

import org.gridsuite.monitor.commons.types.processconfig.LoadFlowConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class LoadFlowConfigValidatorTest {
    @Mock
    private LoadflowRestClient loadflowRestClient;

    private LoadFlowConfigValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LoadFlowConfigValidator(loadflowRestClient);
    }

    @Test
    void getProcessTypeTest() {
        assertThat(validator.getProcessType()).isEqualTo(ProcessType.LOADFLOW);
    }

    @Test
    void validateWithSupportedProvider() {
        UUID parametersUuid = UUID.randomUUID();
        LoadFlowConfig config = new LoadFlowConfig(parametersUuid, List.of());
        when(loadflowRestClient.getParameterProvider(parametersUuid)).thenReturn("OpenLoadFlow");

        assertDoesNotThrow(() -> validator.validate(config));

        verify(loadflowRestClient).getParameterProvider(parametersUuid);
    }

    @Test
    void validateWithUnsupportedProvider() {
        UUID parametersUuid = UUID.randomUUID();
        LoadFlowConfig config = new LoadFlowConfig(parametersUuid, List.of());
        when(loadflowRestClient.getParameterProvider(parametersUuid)).thenReturn("OtherProvider");

        MonitorServerException exception = assertThrows(MonitorServerException.class, () -> validator.validate(config));

        assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_PROVIDER);
        verify(loadflowRestClient).getParameterProvider(parametersUuid);
    }

    @Test
    void validateWithClientFailure() {
        UUID parametersUuid = UUID.randomUUID();
        LoadFlowConfig config = new LoadFlowConfig(parametersUuid, List.of());
        IllegalStateException exception = new IllegalStateException("Loadflow service unavailable");
        when(loadflowRestClient.getParameterProvider(parametersUuid)).thenThrow(exception);

        assertThat(assertThrows(IllegalStateException.class, () -> validator.validate(config))).isSameAs(exception);
    }
}
