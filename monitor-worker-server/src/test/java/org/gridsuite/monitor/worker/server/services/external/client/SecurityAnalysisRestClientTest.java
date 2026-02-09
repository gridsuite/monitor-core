package org.gridsuite.monitor.worker.server.services.external.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.security.SecurityAnalysisResult;
import org.gridsuite.monitor.worker.server.config.MonitorWorkerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@RestClientTest(SecurityAnalysisRestClient.class)
@ContextConfiguration(classes = {MonitorWorkerConfig.class, SecurityAnalysisRestClient.class})
class SecurityAnalysisRestClientTest {
    private static final UUID RESULT_UUID = UUID.randomUUID();

    @Autowired
    private SecurityAnalysisRestClient securityAnalysisRestClient;

    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void saveResult() throws JsonProcessingException {
        SecurityAnalysisResult result = SecurityAnalysisResult.empty();

        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://security-analysis-server/v1/results/" + RESULT_UUID))
            .andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockRestRequestMatchers.content().json(objectMapper.writeValueAsString(result)))
            .andRespond(MockRestResponseCreators.withSuccess());

        assertThatNoException().isThrownBy(() -> securityAnalysisRestClient.saveResult(RESULT_UUID, result));
    }

    @Test
    void saveResultFailed() {
        SecurityAnalysisResult result = SecurityAnalysisResult.empty();
        server.expect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andExpect(MockRestRequestMatchers.requestTo("http://security-analysis-server/v1/results/" + RESULT_UUID))
            .andRespond(MockRestResponseCreators.withServerError());

        assertThatThrownBy(() -> securityAnalysisRestClient.saveResult(RESULT_UUID, result)).isInstanceOf(RestClientException.class);
    }
}
