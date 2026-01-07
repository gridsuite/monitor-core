package org.gridsuite.process.orchestrator.server.services;

import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private StreamBridge publisher;

    @InjectMocks
    private NotificationService notificationService;

    private SecurityAnalysisConfig securityAnalysisConfig;
    private UUID caseUuid;
    private UUID parametersUuid;
    private UUID executionId;

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        parametersUuid = UUID.randomUUID();
        executionId = UUID.randomUUID();

        securityAnalysisConfig = new SecurityAnalysisConfig(
                caseUuid,
                null,
                parametersUuid,
                List.of("contingency1", "contingency2"),
                List.of(UUID.randomUUID(), UUID.randomUUID())
        );
    }

    @Test
    void sendProcessRunMessage() {
        notificationService.sendProcessRunMessage(securityAnalysisConfig, executionId);

        verify(publisher).send(
                eq(ProcessType.SECURITY_ANALYSIS.getBindingName()),
                argThat((ProcessConfig config) -> config.executionId().equals(executionId))
        );
    }
}
