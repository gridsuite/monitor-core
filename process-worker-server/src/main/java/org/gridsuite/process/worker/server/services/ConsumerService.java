package org.gridsuite.process.worker.server.services;

import lombok.RequiredArgsConstructor;
import org.gridsuite.process.commons.ProcessConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ConsumerService {

    private final ProcessExecutionService executionService;

    @Bean
    public Consumer<Message<ProcessConfig>> consumeRun() {
        return message -> executionService.executeProcess(message.getPayload());
    }
}
