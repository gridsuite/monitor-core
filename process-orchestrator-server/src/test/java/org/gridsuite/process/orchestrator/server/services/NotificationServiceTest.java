package org.gridsuite.process.orchestrator.server.services;

import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.commons.ProcessConfig;
import org.gridsuite.process.commons.ProcessType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    void sendprocessrunmessageShouldSendMessageWithCorrectBindingName() {
        // Act
        notificationService.sendProcessRunMessage(securityAnalysisConfig, executionId);

        // Assert
        ArgumentCaptor<String> bindingNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProcessConfig> configCaptor = ArgumentCaptor.forClass(ProcessConfig.class);

        verify(publisher).send(bindingNameCaptor.capture(), configCaptor.capture());

        assertThat(bindingNameCaptor.getValue()).isEqualTo(ProcessType.SECURITY_ANALYSIS.getBindingName());
    }

    @Test
    void sendprocessrunmessageShouldUpdateConfigWithExecutionId() {
        // Act
        notificationService.sendProcessRunMessage(securityAnalysisConfig, executionId);

        // Assert
        ArgumentCaptor<ProcessConfig> configCaptor = ArgumentCaptor.forClass(ProcessConfig.class);
        verify(publisher).send(org.mockito.ArgumentMatchers.anyString(), configCaptor.capture());

        ProcessConfig capturedConfig = configCaptor.getValue();
        assertThat(capturedConfig.executionId()).isEqualTo(executionId);
    }

    @Test
    void sendprocessrunmessageShouldPreserveOriginalConfigData() {
        // Act
        notificationService.sendProcessRunMessage(securityAnalysisConfig, executionId);

        // Assert
        ArgumentCaptor<ProcessConfig> configCaptor = ArgumentCaptor.forClass(ProcessConfig.class);
        verify(publisher).send(org.mockito.ArgumentMatchers.anyString(), configCaptor.capture());

        ProcessConfig capturedConfig = configCaptor.getValue();
        assertThat(capturedConfig.caseUuid()).isEqualTo(caseUuid);
        assertThat(capturedConfig.processType()).isEqualTo(ProcessType.SECURITY_ANALYSIS);

        SecurityAnalysisConfig capturedSaConfig = (SecurityAnalysisConfig) capturedConfig;
        assertThat(capturedSaConfig.parametersUuid()).isEqualTo(parametersUuid);
        assertThat(capturedSaConfig.contingencies()).containsExactly("contingency1", "contingency2");
        assertThat(capturedSaConfig.modificationUuids()).hasSize(2);
    }

}
