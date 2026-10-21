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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class S3ServiceTest {
    @Mock
    S3RestClient s3RestClient;
    @Mock
    FileService fileService;

    S3Service s3Service;

    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3RestClient, fileService);
    }

    @Test
    void testFileIsCompressedBeforeUploading() throws Exception {
        // --- Inputs ---
        String s3Key = "s3Key";
        String uploadedFileName = "debug.xiidm";
        Path source = testDir.resolve(uploadedFileName);
        Files.writeString(source, "hello");

        Path compressedFile = testDir.resolve("compressed.gz");
        Files.writeString(compressedFile, "compressed content");

        when(fileService.compressFile(source)).thenReturn(compressedFile);

        // --- Method invocation ---
        s3Service.compressAndUploadToS3(source, s3Key, uploadedFileName);

        // --- Assertions ---
        verify(fileService).compressFile(source);
        verify(s3RestClient).uploadFile(compressedFile, s3Key, uploadedFileName);
        verify(fileService).deleteTempFile(compressedFile);
    }

    @Test
    void testDirectoryIsCompressedBeforeUploading() throws Exception {
        // --- Inputs ---
        String s3Key = "s3Key";
        String uploadedFileName = "debug.zip";
        Path source = testDir.resolve("debug");
        Files.createDirectory(source);
        Files.writeString(source.resolve("result.txt"), "hello");

        Path compressedFile = testDir.resolve("compressed.zip");
        Files.writeString(compressedFile, "compressed content");

        when(fileService.compressDirectory(source)).thenReturn(compressedFile);

        // --- Method invocation ---
        s3Service.compressAndUploadToS3(source, s3Key, uploadedFileName);

        // --- Assertions ---
        verify(fileService).compressDirectory(source);
        verify(s3RestClient).uploadFile(compressedFile, s3Key, uploadedFileName);
        verify(fileService).deleteTempFile(compressedFile);
    }

    @Test
    void testTempFileIsDeletedEvenIfUploadFails() throws Exception {
        // --- Inputs ---
        String s3Key = "s3Key";
        String uploadedFileName = "debug.xiidm";
        Path source = testDir.resolve(uploadedFileName);
        Files.writeString(source, "hello");

        Path compressedFile = testDir.resolve("compressed.gz");
        Files.writeString(compressedFile, "compressed content");

        when(fileService.compressFile(source)).thenReturn(compressedFile);
        doThrow(new RuntimeException("Upload failed")).when(s3RestClient)
            .uploadFile(eq(compressedFile), anyString(), anyString());

        // --- Method invocation & Assertion ---
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
            () -> s3Service.compressAndUploadToS3(source, s3Key, uploadedFileName));

        // --- Assertions ---
        verify(fileService).deleteTempFile(compressedFile);
    }
}
