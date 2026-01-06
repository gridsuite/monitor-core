/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.process.worker.server.processes.securityanalysis.steps;

import org.gridsuite.process.commons.SecurityAnalysisConfig;
import org.gridsuite.process.worker.server.core.ProcessStepExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test for SAApplyModificationsStep
 *
 * This test serves as an EXAMPLE for testing process-specific steps.
 *
 * Key testing patterns demonstrated:
 * 1. Step type verification
 * 2. Execution with modifications
 * 3. Execution without modifications (skip logic)
 * 4. Execution with empty modification list
 * 5. Execution with null modification list
 * 6. Testing console output (temporary until real implementation)
 *
 * NOTE: This step currently has a TODO implementation that just prints to console.
 * Once the real implementation is added, these tests should be updated to verify
 * actual modification application behavior.
 *
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class SAApplyModificationsStepTest {

    @Mock
    private ProcessStepExecutionContext<SecurityAnalysisConfig> stepContext;

    @Mock
    private SecurityAnalysisConfig config;

    private SAApplyModificationsStep applyModificationsStep;

    @BeforeEach
    void setUp() {
        applyModificationsStep = new SAApplyModificationsStep();

        when(stepContext.getConfig()).thenReturn(config);
    }

    @Test
    void getTypeShouldReturnApplyModifications() {
        // When
        String stepType = applyModificationsStep.getType().getName();

        // Then
        assertEquals("APPLY_MODIFICATIONS", stepType);
    }

    @Test
    void executeShouldProcessModificationsWhenPresent() {
        // Given
        UUID mod1 = UUID.randomUUID();
        UUID mod2 = UUID.randomUUID();
        UUID mod3 = UUID.randomUUID();
        List<UUID> modifications = List.of(mod1, mod2, mod3);

        when(config.modificationUuids()).thenReturn(modifications);

        // Capture console output to verify TODO implementation
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // When
            applyModificationsStep.execute(stepContext);

            // Then
            String output = outputStream.toString();
            assertTrue(output.contains("Applying modifications:"));
            assertTrue(output.contains(mod1.toString()));
            assertTrue(output.contains(mod2.toString()));
            assertTrue(output.contains(mod3.toString()));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void executeShouldNotProcessModificationsWhenNull() {
        // Given
        when(config.modificationUuids()).thenReturn(null);

        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // When
            applyModificationsStep.execute(stepContext);

            // Then - No output should be produced
            String output = outputStream.toString();
            assertFalse(output.contains("Applying modifications:"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void executeShouldNotProcessModificationsWhenEmpty() {
        // Given
        when(config.modificationUuids()).thenReturn(List.of());

        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // When
            applyModificationsStep.execute(stepContext);

            // Then - No output should be produced
            String output = outputStream.toString();
            assertFalse(output.contains("Applying modifications:"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void executeShouldHandleSingleModification() {
        // Given
        UUID singleMod = UUID.randomUUID();
        when(config.modificationUuids()).thenReturn(List.of(singleMod));

        // Capture console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // When
            applyModificationsStep.execute(stepContext);

            // Then
            String output = outputStream.toString();
            assertTrue(output.contains("Applying modifications:"));
            assertTrue(output.contains(singleMod.toString()));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void executeShouldCompleteSuccessfullyWithoutException() {
        // Given
        when(config.modificationUuids()).thenReturn(List.of(UUID.randomUUID()));

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> applyModificationsStep.execute(stepContext));
    }

    @Test
    void executeShouldHandleMutableModificationList() {
        // Given - Use a mutable list
        List<UUID> mutableList = new ArrayList<>();
        mutableList.add(UUID.randomUUID());
        mutableList.add(UUID.randomUUID());

        when(config.modificationUuids()).thenReturn(mutableList);

        // When & Then - Should handle mutable list without issues
        assertDoesNotThrow(() -> applyModificationsStep.execute(stepContext));
    }
}
