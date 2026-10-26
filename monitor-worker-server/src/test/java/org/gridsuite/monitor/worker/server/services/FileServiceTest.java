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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileServiceTest {

    private final FileService fileService = new FileService();

    @TempDir
    Path tempDir;

    @Test
    void testWithTempFile() throws Exception {
        AtomicReference<Path> createdFile = new AtomicReference<>();
        fileService.withTempFile("prefix", ".txt", tempFile -> {
            createdFile.set(tempFile);
            assertThat(tempFile).exists();
            assertThat(tempFile.getFileName().toString()).startsWith("prefix").endsWith(".txt");
            assertThat(tempFile.getParent().getFileName().toString()).startsWith("process-debug");
        });
        assertThat(createdFile.get()).doesNotExist();
        assertThat(createdFile.get().getParent()).doesNotExist();
    }

    @Test
    void testWithTempFileCleansUpOnFailure() {
        AtomicReference<Path> createdFile = new AtomicReference<>();
        IOException failure = new IOException("Writing failed");

        assertSame(failure, assertThrows(IOException.class, () ->
            fileService.withTempFile("prefix", ".txt", tempFile -> {
                createdFile.set(tempFile);
                throw failure;
            })));

        assertThat(createdFile.get()).doesNotExist();
        assertThat(createdFile.get().getParent()).doesNotExist();
    }

    @Test
    void testCompressFile() throws Exception {
        Path sourceFile = tempDir.resolve("source.txt");
        Files.writeString(sourceFile, "hello world");

        fileService.withTempFile("compressed", ".gz", compressedFile -> {
            fileService.compressFile(sourceFile, compressedFile);
            assertThat(compressedFile).exists();
            assertThat(compressedFile.getFileName().toString()).endsWith(".gz");

            try (InputStream in = new GZIPInputStream(Files.newInputStream(compressedFile))) {
                String content = new String(in.readAllBytes(), UTF_8);
                assertThat(content).isEqualTo("hello world");
            }
        });
    }

    @Test
    void testCompressDirectory() throws Exception {
        Path sourceDir = tempDir.resolve("sourceDir");
        Files.createDirectory(sourceDir);
        Files.writeString(sourceDir.resolve("file1.txt"), "content1");

        fileService.withTempFile("compressed", ".zip", compressedFile -> {
            fileService.compressDirectory(sourceDir, compressedFile);
            assertThat(compressedFile).exists();
            assertThat(compressedFile.getFileName().toString()).endsWith(".zip");

            try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(compressedFile))) {
                ZipEntry entry = zin.getNextEntry();
                assertThat(entry).isNotNull();
                assertThat(entry.getName()).isEqualTo("file1.txt");
                String content = new String(zin.readAllBytes(), UTF_8);
                assertThat(content).isEqualTo("content1");
            }
        });
    }

    @Test
    void testWithDebugDir() {
        AtomicReference<Path> createdDir = new AtomicReference<>();
        fileService.withDebugDir(ShortCircuitStepType.RUN_SC_COMPUTATION, debugDir -> {
            createdDir.set(debugDir);
            assertThat(debugDir).exists();
            assertThat(debugDir.getFileName().toString()).contains(ShortCircuitStepType.RUN_SC_COMPUTATION.getName().toLowerCase() + "_debug_");
            try {
                Files.writeString(Files.createDirectory(debugDir.resolve("nested")).resolve("debug.txt"), "debug");
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        });
        assertThat(createdDir.get()).doesNotExist();
        assertThat(createdDir.get().getParent()).exists();
    }

    @Test
    void testWithDebugDirCleansUpOnFailure() {
        AtomicReference<Path> createdDir = new AtomicReference<>();
        RuntimeException failure = new RuntimeException("Computation failed");

        assertSame(failure, assertThrows(RuntimeException.class, () ->
            fileService.withDebugDir(ShortCircuitStepType.RUN_SC_COMPUTATION, debugDir -> {
                createdDir.set(debugDir);
                throw failure;
            })));

        assertThat(createdDir.get()).doesNotExist();
        assertThat(createdDir.get().getParent()).exists();
    }
}
