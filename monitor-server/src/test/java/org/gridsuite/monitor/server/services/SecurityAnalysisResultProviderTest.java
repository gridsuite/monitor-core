/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.ResultType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
class SecurityAnalysisResultProviderTest {
    private final SecurityAnalysisService securityAnalysisService =
        Mockito.mock(SecurityAnalysisService.class);

    private final SecurityAnalysisResultProvider provider =
        new SecurityAnalysisResultProvider(securityAnalysisService);

    @Test
    void getTypeShouldReturnSecurityAnalysis() {
        assertThat(provider.getType())
            .isEqualTo(ResultType.SECURITY_ANALYSIS);
    }

    @Test
    void getResultShouldDelegateToSecurityAnalysisService() {
        UUID id = UUID.randomUUID();
        String expected = "result";

        when(securityAnalysisService.getResult(id)).thenReturn(expected);

        String result = provider.getResult(id);

        assertThat(result).isEqualTo(expected);
        verify(securityAnalysisService).getResult(id);
        verifyNoMoreInteractions(securityAnalysisService);
    }
}
