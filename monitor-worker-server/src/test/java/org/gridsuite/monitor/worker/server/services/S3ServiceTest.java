/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.worker.server.clients.S3RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.function.ThrowingConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class S3ServiceTest {
    @Mock
    S3RestClient s3RestClient;
    @Spy
    FileService fileService = new FileService();
    @Mock
    ThrowingConsumer<Path> writer;

    S3Service s3Service;

    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3RestClient, fileService);
    }

    @Test
    void testExportWritesBeforeCompressingAndUploading() throws Exception {
        doAnswer(invocation -> {
            Path compressedFile = invocation.getArgument(0);
            try (var input = new GZIPInputStream(Files.newInputStream(compressedFile))) {
                assertEquals("network", new String(input.readAllBytes(), UTF_8));
            }
            return null;
        }).when(s3RestClient).uploadFile(any(), eq("s3Key"), eq("debug.xiidm.gz"));

        s3Service.exportCompressedToS3("s3Key", "debug", ".xiidm", path -> Files.writeString(path, "network"));

        ArgumentCaptor<Path> source = ArgumentCaptor.captor();
        ArgumentCaptor<Path> compressedFile = ArgumentCaptor.captor();
        verify(fileService).compressFile(source.capture(), compressedFile.capture());
        verify(s3RestClient).uploadFile(compressedFile.getValue(), "s3Key", "debug.xiidm.gz");
        assertTemporaryFileDeleted(source.getValue());
        assertTemporaryFileDeleted(compressedFile.getValue());
    }

    @Test
    void testExportCleansUpWhenWriterFails() throws Exception {
        RuntimeException failure = new RuntimeException("Export failed");
        doThrow(failure).when(writer).accept(any());

        assertSame(failure, assertThrows(RuntimeException.class,
            () -> s3Service.exportCompressedToS3("s3Key", "debug", ".xiidm", writer)));

        ArgumentCaptor<Path> source = ArgumentCaptor.captor();
        verify(writer).accept(source.capture());
        assertTemporaryFileDeleted(source.getValue());
        verify(fileService, never()).compressFile(any(), any());
        verifyNoInteractions(s3RestClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"compression", "upload"})
    void testExportCleansUpAndPropagatesFailure(String failingStage) throws Exception {
        IOException failure = new IOException("Export failed");
        switch (failingStage) {
            case "compression" -> doThrow(failure).when(fileService).compressFile(any(), any());
            case "upload" -> doThrow(failure).when(s3RestClient).uploadFile(any(), eq("s3Key"), eq("debug.xiidm.gz"));
            default -> throw new IllegalArgumentException(failingStage);
        }

        assertSame(failure, assertThrows(IOException.class,
            () -> s3Service.exportCompressedToS3("s3Key", "debug", ".xiidm", writer)));

        ArgumentCaptor<Path> source = ArgumentCaptor.captor();
        ArgumentCaptor<Path> compressedFile = ArgumentCaptor.captor();
        verify(fileService).compressFile(source.capture(), compressedFile.capture());
        assertTemporaryFileDeleted(source.getValue());
        assertTemporaryFileDeleted(compressedFile.getValue());
        if ("compression".equals(failingStage)) {
            verifyNoInteractions(s3RestClient);
        }
    }

    @Test
    void testFileIsCompressedBeforeUploading() throws Exception {
        // --- Inputs ---
        String s3Key = "s3Key";
        String uploadedFileName = "debug.xiidm";
        Path source = testDir.resolve(uploadedFileName);
        Files.writeString(source, "hello");

        // --- Method invocation ---
        s3Service.compressAndUploadToS3(source, s3Key, uploadedFileName);

        // --- Assertions ---
        ArgumentCaptor<Path> compressedFile = ArgumentCaptor.captor();
        verify(s3RestClient).uploadFile(compressedFile.capture(), eq(s3Key), eq(uploadedFileName));
        verify(fileService).compressFile(source, compressedFile.getValue());
        assertTemporaryFileDeleted(compressedFile.getValue());
        assertTrue(Files.exists(source));
    }

    @Test
    void testDirectoryIsCompressedBeforeUploading() throws Exception {
        // --- Inputs ---
        String s3Key = "s3Key";
        String uploadedFileName = "debug.zip";
        Path source = testDir.resolve("debug");
        Files.createDirectory(source);
        Files.writeString(source.resolve("result.txt"), "hello");

        // --- Method invocation ---
        s3Service.compressAndUploadToS3(source, s3Key, uploadedFileName);

        // --- Assertions ---
        ArgumentCaptor<Path> compressedFile = ArgumentCaptor.captor();
        verify(s3RestClient).uploadFile(compressedFile.capture(), eq(s3Key), eq(uploadedFileName));
        verify(fileService).compressDirectory(source, compressedFile.getValue());
        assertTemporaryFileDeleted(compressedFile.getValue());
        assertTrue(Files.exists(source.resolve("result.txt")));
    }

    @Test
    void testTempFileIsDeletedEvenIfUploadFails() throws Exception {
        // --- Inputs ---
        String s3Key = "s3Key";
        String uploadedFileName = "debug.xiidm";
        Path source = testDir.resolve(uploadedFileName);
        Files.writeString(source, "hello");

        doThrow(new RuntimeException("Upload failed")).when(s3RestClient)
            .uploadFile(any(), anyString(), anyString());

        // --- Method invocation & Assertion ---
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
            () -> s3Service.compressAndUploadToS3(source, s3Key, uploadedFileName));

        // --- Assertions ---
        ArgumentCaptor<Path> compressedFile = ArgumentCaptor.captor();
        verify(s3RestClient).uploadFile(compressedFile.capture(), eq(s3Key), eq(uploadedFileName));
        assertTemporaryFileDeleted(compressedFile.getValue());
        assertTrue(Files.exists(source));
    }

    private void assertTemporaryFileDeleted(Path path) {
        assertFalse(Files.exists(path));
        assertFalse(Files.exists(path.getParent()));
    }
}
