/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.worker.server.process.shortcircuit.ShortCircuitStepType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class FileServiceTest {

    private final FileService fileService = new FileService();

    @TempDir
    Path tempDir;

    @Test
    void testCreateTempFile() throws Exception {
        Path tempFile = fileService.createTempFile("prefix", ".txt");
        try {
            assertThat(tempFile).exists();
            assertThat(tempFile.getFileName().toString()).startsWith("prefix").endsWith(".txt");
            assertThat(tempFile.getParent().getFileName().toString()).startsWith("process-debug");
        } finally {
            fileService.deleteTempFile(tempFile);
        }
    }

    @Test
    void testCompressFile() throws Exception {
        Path sourceFile = tempDir.resolve("source.txt");
        Files.writeString(sourceFile, "hello world");

        Path compressedFile = fileService.compressFile(sourceFile);
        try {
            assertThat(compressedFile).exists();
            assertThat(compressedFile.getFileName().toString()).endsWith(".gz");

            try (InputStream in = new GZIPInputStream(Files.newInputStream(compressedFile))) {
                String content = new String(in.readAllBytes(), UTF_8);
                assertThat(content).isEqualTo("hello world");
            }
        } finally {
            fileService.deleteTempFile(compressedFile);
        }
    }

    @Test
    void testCompressDirectory() throws Exception {
        Path sourceDir = tempDir.resolve("sourceDir");
        Files.createDirectory(sourceDir);
        Files.writeString(sourceDir.resolve("file1.txt"), "content1");

        Path compressedFile = fileService.compressDirectory(sourceDir);
        try {
            assertThat(compressedFile).exists();
            assertThat(compressedFile.getFileName().toString()).endsWith(".zip");

            try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(compressedFile))) {
                ZipEntry entry = zin.getNextEntry();
                assertThat(entry).isNotNull();
                assertThat(entry.getName()).isEqualTo("file1.txt");
                String content = new String(zin.readAllBytes(), UTF_8);
                assertThat(content).isEqualTo("content1");
            }
        } finally {
            fileService.deleteTempFile(compressedFile);
        }
    }

    @Test
    void testDeleteTempFile() throws Exception {
        Path tempFile = fileService.createTempFile("prefix", ".txt");
        Path parentDir = tempFile.getParent();

        fileService.deleteTempFile(tempFile);

        assertThat(tempFile).doesNotExist();
        assertThat(parentDir).doesNotExist();
    }

    @Test
    void testCreateDebugDir() {
        Path debugDir = fileService.createDebugDir(ShortCircuitStepType.RUN_SC_COMPUTATION);
        try {
            assertThat(debugDir).exists();
            assertThat(debugDir.getFileName().toString()).contains(ShortCircuitStepType.RUN_SC_COMPUTATION.getName().toLowerCase() + "_debug_");
        } finally {
            fileService.deleteTempFile(debugDir);
        }
    }
}
