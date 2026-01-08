package org.gridsuite.process.worker.server.processes.securityanalysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.process.commons.ResultInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DummySecurityAnalysisService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummySecurityAnalysisService.class);
    private final ObjectMapper objectMapper;

    public DummySecurityAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void saveResult(ResultInfos resultInfos, Object result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            LOGGER.info("saved results uuid : {} content : {}...", resultInfos.resultUUID(), resultJson.substring(0, Math.min(resultJson.length(), 500)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
