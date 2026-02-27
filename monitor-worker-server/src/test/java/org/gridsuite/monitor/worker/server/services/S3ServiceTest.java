/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.function.ThrowingConsumer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;
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
    S3RestService s3RestService;

    @InjectMocks
    S3Service s3Service;

    @TempDir
    Path testDir;

    @Test
    void testPathIsCompressedBeforeUploading() throws Exception {
        // --- Inputs ---
        Network network = EurostagTutorialExample1Factory.create();
        String s3Key = "s3Key";
        String fileNamePrefix = "fileName";
        String fileNameSuffix = ".xiidm";
        ThrowingConsumer<Path> writer =
            path -> network.write("XIIDM", null, path);

        // write network directly in expectedFile.xiidm for later assertions
        Path expectedFilePath = testDir.resolve("expectedFile.xiidm");
        network.write("XIIDM", null, expectedFilePath);

        // Since temp files are deleted after "uploadFile", make a copy of the compressed file to assert its content
        Path capturedCopy = Files.createTempFile(testDir, "test-copy", ".gz");
        doAnswer(invocation -> {
            Path uploaded = invocation.getArgument(0);
            Files.copy(uploaded, capturedCopy, StandardCopyOption.REPLACE_EXISTING);
            return null;
        }).when(s3RestService)
            .uploadFile(any(Path.class), anyString(), anyString());

        // --- Method invocation ---
        s3Service.exportCompressedToS3(s3Key, fileNamePrefix, fileNameSuffix, writer);

        // --- Assertions ---
        verify(s3RestService).uploadFile(any(), eq(s3Key), eq(String.join("", fileNamePrefix, fileNameSuffix, ".gz")));
        try (InputStream in = new GZIPInputStream(Files.newInputStream(capturedCopy))) {
            String uncompressedContent = new String(in.readAllBytes(), UTF_8);
            assertThat(uncompressedContent).isNotNull();
            assertThat(uncompressedContent).isEqualTo(Files.readString(expectedFilePath));
        }
    }

    @Test
    void testTempFilesAreDeletedAfterExecution() throws Exception {
        // --- Inputs ---
        AtomicReference<Path> debugFileToUplad = new AtomicReference<>();
        AtomicReference<Path> debugTempDir = new AtomicReference<>();
        String s3Key = "s3Key";
        String fileNamePrefix = "fileName";
        String fileNameSuffix = ".test";

        ThrowingConsumer<Path> writer = path -> {
            debugFileToUplad.set(path);
            debugTempDir.set(path.getParent());
            Files.writeString(path, "hello");
        };

        // --- Method invocation ---
        s3Service.exportCompressedToS3(s3Key, fileNamePrefix, fileNameSuffix, writer);

        // --- Assertions ---
        ArgumentCaptor<Path> compressedDebugFileToUploadCaptor = ArgumentCaptor.forClass(Path.class);
        verify(s3RestService).uploadFile(
            compressedDebugFileToUploadCaptor.capture(),
            eq(s3Key),
            eq(String.join("", fileNamePrefix, fileNameSuffix, ".gz"))
        );
        Path compressedDebugFileToUpload = compressedDebugFileToUploadCaptor.getValue();

        assertThat(Files.exists(compressedDebugFileToUpload)).isFalse();
        assertThat(Files.exists(debugFileToUplad.get())).isFalse();
        assertThat(Files.exists(debugTempDir.get())).isFalse();
    }
}
