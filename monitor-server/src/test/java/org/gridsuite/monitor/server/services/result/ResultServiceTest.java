/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result;

import org.gridsuite.monitor.commons.api.types.result.ResultInfos;
import org.gridsuite.monitor.commons.api.types.result.ResultType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultProvider resultProvider;

    private ResultService resultService;

    @Test
    void getResultShouldReturnResultFromCorrectProvider() {
        UUID resultId = UUID.randomUUID();
        String expectedResult = "{\"result\": \"security analysis data\"}";
        when(resultProvider.getResult(resultId)).thenReturn(expectedResult);
        when(resultProvider.getType()).thenReturn(ResultType.SECURITY_ANALYSIS);
        resultService = new ResultService(List.of(resultProvider));
        ResultInfos resultInfos = new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS);

        String result = resultService.getResult(resultInfos);

        assertThat(result).isEqualTo(expectedResult);
        verify(resultProvider).getResult(resultId);
    }

    @Test
    void getResultShouldThrowExceptionWhenProviderNotFound() {
        UUID resultId = UUID.randomUUID();
        ResultInfos resultInfos = new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS);

        ResultService emptyService = new ResultService(List.of());

        assertThatThrownBy(() -> emptyService.getResult(resultInfos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported result type: " + ResultType.SECURITY_ANALYSIS);
    }

    @Test
    void deleteResultShouldDeleteFromCorrectProvider() {
        UUID resultId = UUID.randomUUID();
        doNothing().when(resultProvider).deleteResult(resultId);
        when(resultProvider.getType()).thenReturn(ResultType.SECURITY_ANALYSIS);
        resultService = new ResultService(List.of(resultProvider));
        ResultInfos resultInfos = new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS);

        resultService.deleteResult(resultInfos);

        verify(resultProvider).deleteResult(resultId);
    }

    @Test
    void deleteResultShouldThrowExceptionWhenProviderNotFound() {
        UUID resultId = UUID.randomUUID();
        ResultInfos resultInfos = new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS);

        ResultService emptyService = new ResultService(List.of());

        assertThatThrownBy(() -> emptyService.deleteResult(resultInfos))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported result type: " + ResultType.SECURITY_ANALYSIS);
    }
}
