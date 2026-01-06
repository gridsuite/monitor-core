package org.gridsuite.process.orchestrator.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.process.commons.ProcessConfig;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final StreamBridge publisher;

    public void sendProcessRunMessage(ProcessConfig processConfig, UUID executionId) {
        publisher.send(
            processConfig.processType().getBindingName(),
            processConfig.withExecutionId(executionId)
        );
    }
}
