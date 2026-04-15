/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services.result;

import org.gridsuite.monitor.commons.types.result.ResultInfos;
import org.gridsuite.monitor.commons.types.result.ResultType;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.UNSUPPORTED_RESULT_TYPE;
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

        MonitorServerException exception = (MonitorServerException) catchThrowable(() -> emptyService.getResult(resultInfos));
        assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_RESULT_TYPE);
        assertThat(exception.getBusinessErrorValues()).containsEntry("resultType", ResultType.SECURITY_ANALYSIS);
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

        MonitorServerException exception = (MonitorServerException) catchThrowable(() -> emptyService.deleteResult(resultInfos));
        assertThat(exception.getErrorCode()).isEqualTo(UNSUPPORTED_RESULT_TYPE);
        assertThat(exception.getBusinessErrorValues()).containsEntry("resultType", ResultType.SECURITY_ANALYSIS);
    }
}
