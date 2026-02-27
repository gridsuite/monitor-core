/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result.providers;

import org.gridsuite.monitor.commons.api.types.result.ResultType;
import org.gridsuite.monitor.server.clients.SecurityAnalysisRestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
class SecurityAnalysisResultProviderTest {
    private final SecurityAnalysisRestClient securityAnalysisRestClient =
        Mockito.mock(SecurityAnalysisRestClient.class);

    private final SecurityAnalysisResultProvider provider =
        new SecurityAnalysisResultProvider(securityAnalysisRestClient);

    @Test
    void getTypeShouldReturnSecurityAnalysis() {
        assertThat(provider.getType())
            .isEqualTo(ResultType.SECURITY_ANALYSIS);
    }

    @Test
    void getResultShouldDelegateToSecurityAnalysisService() {
        UUID id = UUID.randomUUID();
        String expected = "result";

        when(securityAnalysisRestClient.getResult(id)).thenReturn(expected);

        String result = provider.getResult(id);

        assertThat(result).isEqualTo(expected);
        verify(securityAnalysisRestClient).getResult(id);
        verifyNoMoreInteractions(securityAnalysisRestClient);
    }

    @Test
    void deleteResultShouldDelegateToSecurityAnalysisService() {
        UUID id = UUID.randomUUID();

        doNothing().when(securityAnalysisRestClient).deleteResult(id);

        provider.deleteResult(id);

        verify(securityAnalysisRestClient).deleteResult(id);
        verifyNoMoreInteractions(securityAnalysisRestClient);
    }
}
