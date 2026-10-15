/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.services.processconfig.validators.ProcessConfigValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_PROVIDER;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessConfigValidationServiceTest {
    @Mock
    private ProcessConfigValidator<ProcessConfig> validator;
    @Mock
    private ProcessConfigValidator<ProcessConfig> otherValidator;
    @Mock
    private ProcessConfig processConfig;

    @ParameterizedTest
    @EnumSource(ProcessType.class)
    void validate(ProcessType processType) {
        ProcessType otherType = processType == ProcessType.LOADFLOW ? ProcessType.SECURITY_ANALYSIS : ProcessType.LOADFLOW;
        when(validator.getProcessType()).thenReturn(processType);
        when(otherValidator.getProcessType()).thenReturn(otherType);
        when(processConfig.processType()).thenReturn(processType);
        ProcessConfigValidationService service = new ProcessConfigValidationService(List.of(validator, otherValidator));

        service.validate(processConfig);

        verify(validator).validate(processConfig);
        verify(otherValidator, never()).validate(any());
    }

    @Test
    void duplicateValidators() {
        when(validator.getProcessType()).thenReturn(ProcessType.LOADFLOW);
        when(otherValidator.getProcessType()).thenReturn(ProcessType.LOADFLOW);
        List<ProcessConfigValidator<?>> sameProcessTypeValidators = List.of(validator, otherValidator);

        assertThatThrownBy(() -> new ProcessConfigValidationService(sameProcessTypeValidators))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Duplicate process config validators for process type: LOADFLOW");
    }

    @Test
    void unsupportedProcessType() {
        when(validator.getProcessType()).thenReturn(ProcessType.LOADFLOW);
        when(processConfig.processType()).thenReturn(ProcessType.SECURITY_ANALYSIS);
        ProcessConfigValidationService service = new ProcessConfigValidationService(List.of(validator));

        assertThatThrownBy(() -> service.validate(processConfig))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported process config type: SECURITY_ANALYSIS");
        verify(validator, never()).validate(any());
    }

    @Test
    void noValidators() {
        when(processConfig.processType()).thenReturn(ProcessType.LOADFLOW);
        ProcessConfigValidationService service = new ProcessConfigValidationService(List.of());

        assertThatThrownBy(() -> service.validate(processConfig))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported process config type: LOADFLOW");
    }

    @Test
    void validationFailure() {
        when(validator.getProcessType()).thenReturn(ProcessType.LOADFLOW);
        when(processConfig.processType()).thenReturn(ProcessType.LOADFLOW);
        MonitorServerException exception = new MonitorServerException(UNSUPPORTED_PROVIDER, "Invalid provider");
        doThrow(exception).when(validator).validate(processConfig);
        ProcessConfigValidationService service = new ProcessConfigValidationService(List.of(validator));

        assertThatThrownBy(() -> service.validate(processConfig)).isSameAs(exception);
        verify(validator).validate(processConfig);
    }
}
