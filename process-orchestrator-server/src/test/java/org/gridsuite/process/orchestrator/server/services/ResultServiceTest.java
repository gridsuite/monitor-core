package org.gridsuite.process.orchestrator.server.services;

import org.gridsuite.process.commons.ResultType;
import org.gridsuite.process.commons.ResultInfos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultProvider resultProvider;

    private ResultService resultService;

    @Test
    void getResultShouldReturnResultFromCorrectProvider() {
        // Arrange
        UUID resultId = UUID.randomUUID();
        String expectedResult = "{\"result\": \"security analysis data\"}";
        when(resultProvider.getResult(resultId)).thenReturn(expectedResult);
        when(resultProvider.getType()).thenReturn(ResultType.SECURITY_ANALYSIS);
        resultService = new ResultService(List.of(resultProvider));

        ResultInfos resultInfos = new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS);

        // Act
        String result = resultService.getResult(resultInfos);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(resultProvider).getResult(resultId);
    }

    @Test
    void getResultShouldThrowExceptionWhenProviderNotFound() {
        // Arrange
        UUID resultId = UUID.randomUUID();
        ResultInfos resultInfos = new ResultInfos(resultId, ResultType.SECURITY_ANALYSIS);

        // Create a service with empty providers list to simulate unsupported type
        ResultService emptyService = new ResultService(List.of());

        // Act & Assert
        assertThatThrownBy(() -> emptyService.getResult(resultInfos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported result type: " + ResultType.SECURITY_ANALYSIS);
    }
}
