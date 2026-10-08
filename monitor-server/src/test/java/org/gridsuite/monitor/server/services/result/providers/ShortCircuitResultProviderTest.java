/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result.providers;

import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.clients.ShortCircuitRestClient;
import org.gridsuite.monitor.server.services.result.ResultQueryParams;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
class ShortCircuitResultProviderTest {
    private final ShortCircuitRestClient shortCircuitRestClient = mock(ShortCircuitRestClient.class);
    private final ShortCircuitResultProvider provider = new ShortCircuitResultProvider(shortCircuitRestClient);

    @Test
    void getTypeShouldReturnShortCircuit() {
        assertThat(provider.getType())
            .isEqualTo(ResultType.SHORT_CIRCUIT);
    }

    @Test
    void getResultShouldDelegateToShortCircuitService() {
        UUID id = UUID.randomUUID();
        String expected = "result";

        when(shortCircuitRestClient.getResult(id)).thenReturn(expected);
        when(shortCircuitRestClient.getResult(id, null)).thenReturn(expected);

        String result = provider.getResult(id);

        assertThat(result).isEqualTo(expected);
        verify(shortCircuitRestClient).getResult(id);
        verifyNoMoreInteractions(shortCircuitRestClient);
    }

    @Test
    void getPagedResultShouldDelegateQueryToShortCircuitService() {
        UUID id = UUID.randomUUID();
        ResultQueryParams queryParams = new ResultQueryParams("ALL_BUSES", 0, 25, java.util.List.of(), null);
        when(shortCircuitRestClient.getResult(id, queryParams)).thenReturn("paged-result");

        assertThat(provider.getResult(id, queryParams)).isEqualTo("paged-result");

        verify(shortCircuitRestClient).getResult(id, queryParams);
        verifyNoMoreInteractions(shortCircuitRestClient);
    }

    @Test
    void deleteResultShouldDelegateToShortCircuitService() {
        UUID id = UUID.randomUUID();

        doNothing().when(shortCircuitRestClient).deleteResult(id);

        provider.deleteResult(id);

        verify(shortCircuitRestClient).deleteResult(id);
        verifyNoMoreInteractions(shortCircuitRestClient);
    }
}
