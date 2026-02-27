/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import org.gridsuite.monitor.commons.ProcessConfig;
import org.gridsuite.monitor.commons.ProcessRunMessage;
import org.gridsuite.monitor.commons.SecurityAnalysisConfig;
import org.gridsuite.monitor.commons.SnapshotRefinerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private StreamBridge publisher;

    @InjectMocks
    private NotificationService notificationService;

    private UUID caseUuid;
    private UUID executionId;

    private static Stream<Arguments> provideProcessConfig() {
        return Stream.of(
                Arguments.of(new SecurityAnalysisConfig(
                                UUID.randomUUID(),
                                List.of("contingency1", "contingency2"),
                                List.of(UUID.randomUUID())),
                        "publishRunSecurityAnalysis-out-0"
                ),
                Arguments.of(new SnapshotRefinerConfig(
                                Optional.of(UUID.randomUUID()),
                                Optional.of(UUID.randomUUID())),
                        "publishRunSnapshotRefiner-out-0"
                )
        );
    }

    @BeforeEach
    void setUp() {
        caseUuid = UUID.randomUUID();
        executionId = UUID.randomUUID();
    }

    @ParameterizedTest
    @MethodSource("provideProcessConfig")
    void sendProcessRunMessage(ProcessConfig processConfig, String expectedBindingName) {
        String debugFileLocation = "debug/file/location";
        notificationService.sendProcessRunMessage(caseUuid, processConfig, executionId, debugFileLocation);

        verify(publisher).send(
                eq(expectedBindingName),
                argThat((ProcessRunMessage<?> message) ->
                        message.executionId().equals(executionId) &&
                        message.caseUuid().equals(caseUuid) &&
                        message.config().equals(processConfig) &&
                        message.debugFileLocation().equals(debugFileLocation))
        );
    }
}
