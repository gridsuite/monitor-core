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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@ExtendWith(MockitoExtension.class)
class S3ServiceTest {
    @Mock
    S3RestClient s3RestClient;

    S3Service s3Service;

    @TempDir
    Path testDir;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3RestClient, new FileService());
    }

    @Test
    void testFileIsCompressedBeforeUploading() throws Exception {
        // --- Inputs ---
        String s3Key = "s3Key";
        String uploadedFileName = "debug.xiidm";
        Path source = testDir.resolve(uploadedFileName);
        Files.writeString(source, "hello");

        // Since temp files are deleted after "uploadFile", make a copy of the compressed file to assert its content
        Path capturedCopy = Files.createTempFile(testDir, "test-copy", ".gz");
        doAnswer(invocation -> {
            Path uploaded = invocation.getArgument(0);
            Files.copy(uploaded, capturedCopy, StandardCopyOption.REPLACE_EXISTING);
            return null;
        }).when(s3RestClient)
            .uploadFile(any(Path.class), anyString(), anyString());

        // --- Method invocation ---
        s3Service.compressAndUploadToS3(source, s3Key, uploadedFileName);

        // --- Assertions ---
        verify(s3RestClient).uploadFile(any(), eq(s3Key), eq(uploadedFileName));
        try (InputStream in = new GZIPInputStream(Files.newInputStream(capturedCopy))) {
            String uncompressedContent = new String(in.readAllBytes(), UTF_8);
            assertThat(uncompressedContent).isNotNull();
            assertThat(uncompressedContent).isEqualTo(Files.readString(source));
        }
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
        ArgumentCaptor<Path> compressedDebugFileToUploadCaptor = ArgumentCaptor.forClass(Path.class);
        verify(s3RestClient).uploadFile(
            compressedDebugFileToUploadCaptor.capture(),
            eq(s3Key),
            eq(uploadedFileName)
        );
        Path compressedDebugFileToUpload = compressedDebugFileToUploadCaptor.getValue();

        assertThat(Files.exists(compressedDebugFileToUpload)).isFalse();
    }
}
