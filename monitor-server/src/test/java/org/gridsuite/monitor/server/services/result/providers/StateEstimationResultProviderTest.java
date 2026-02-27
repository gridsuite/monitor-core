/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result.providers;

import org.gridsuite.monitor.commons.api.types.result.ResultType;
import org.gridsuite.monitor.server.clients.StateEstimationRestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Achour BERRAHMA <achour.berrahma at rte-france.com>
 */
class StateEstimationResultProviderTest {
    private final StateEstimationRestClient stateEstimationRestClient =
            Mockito.mock(StateEstimationRestClient.class);

    private final StateEstimationResultProvider provider =
            new StateEstimationResultProvider(stateEstimationRestClient);

    @Test
    void getTypeShouldReturnStateEstimation() {
        assertThat(provider.getType())
                .isEqualTo(ResultType.STATE_ESTIMATION);
    }

    @Test
    void getResultShouldDelegateToStateEstimationService() {
        UUID id = UUID.randomUUID();
        String expected = "result";

        when(stateEstimationRestClient.getResult(id)).thenReturn(expected);

        String result = provider.getResult(id);

        assertThat(result).isEqualTo(expected);
        verify(stateEstimationRestClient).getResult(id);
        verifyNoMoreInteractions(stateEstimationRestClient);
    }

    @Test
    void deleteResultShouldDelegateToStateEstimationService() {
        UUID id = UUID.randomUUID();

        doNothing().when(stateEstimationRestClient).deleteResult(id);

        provider.deleteResult(id);

        verify(stateEstimationRestClient).deleteResult(id);
        verifyNoMoreInteractions(stateEstimationRestClient);
    }
}
