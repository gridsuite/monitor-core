/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result.providers;

import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.clients.LoadflowRestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Caroline Jeandat {@literal <caroline.jeandat at rte-france.com>}
 */
class LoadflowResultProviderTest {
    private final LoadflowRestClient loadflowRestClient = Mockito.mock(LoadflowRestClient.class);
    private final LoadflowResultProvider provider = new LoadflowResultProvider(loadflowRestClient);

    @Test
    void getTypeShouldReturnLoadflow() {
        assertThat(provider.getType())
            .isEqualTo(ResultType.LOADFLOW);
    }

    @Test
    void getResultShouldDelegateToLoadflowService() {
        UUID id = UUID.randomUUID();
        String expected = "result";

        when(loadflowRestClient.getResult(id)).thenReturn(expected);

        String result = provider.getResult(id);

        assertThat(result).isEqualTo(expected);
        verify(loadflowRestClient).getResult(id);
        verifyNoMoreInteractions(loadflowRestClient);
    }

    @Test
    void deleteResultShouldDelegateToLoadflowService() {
        UUID id = UUID.randomUUID();

        doNothing().when(loadflowRestClient).deleteResult(id);

        provider.deleteResult(id);

        verify(loadflowRestClient).deleteResult(id);
        verifyNoMoreInteractions(loadflowRestClient);
    }
}
