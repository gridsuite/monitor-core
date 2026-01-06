package org.gridsuite.process.orchestrator.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.process.commons.ResultType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class DummySecurityAnalysisService implements ResultProvider {

    private final ObjectMapper objectMapper;

    public DummySecurityAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ResultType getType() {
        return ResultType.SECURITY_ANALYSIS;
    }

    @Override
    public String getResult(UUID resultId) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "resultId", resultId,
                "type", "SECURITY_ANALYSIS",
                "status", "DUMMY_RESULT",
                "content", "This is a dummy security analysis result for ID " + resultId
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
