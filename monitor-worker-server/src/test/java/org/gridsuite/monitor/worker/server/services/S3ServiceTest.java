package org.gridsuite.monitor.worker.server.services;

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
        String s3Key = "s3Key";
        String fileName = "fileName";
        String dataToCompress = "dataToCompress";
        ThrowingConsumer<Path> writer =
            path -> Files.writeString(path, dataToCompress);

        // Since temp files are deleted after "uploadFile", make a copy of the compressed file to assert its content
        Path capturedCopy = Files.createTempFile(testDir, "test-copy", ".gz");
        doAnswer(invocation -> {
            Path uploaded = invocation.getArgument(0);
            Files.copy(uploaded, capturedCopy, StandardCopyOption.REPLACE_EXISTING);
            return null;
        }).when(s3RestService)
            .uploadFile(any(Path.class), anyString(), anyString());

        // --- Method invocation ---
        s3Service.exportCompressedToS3(s3Key, fileName, writer);

        // --- Assertions ---
        verify(s3RestService).uploadFile(any(), eq(s3Key), eq(fileName));
        try (InputStream in = new GZIPInputStream(Files.newInputStream(capturedCopy))) {
            String uncompressedContent = new String(in.readAllBytes(), UTF_8);
            assertThat(uncompressedContent).isEqualTo(dataToCompress);
        }
    }

    @Test
    void testTempFilesAreDeletedAfterExecution() throws Exception {
        // --- Inputs ---
        AtomicReference<Path> debugFileToUplad = new AtomicReference<>();
        AtomicReference<Path> debugTempDir = new AtomicReference<>();
        String s3Key = "s3Key";
        String fileName = "fileName";

        ThrowingConsumer<Path> writer = path -> {
            debugFileToUplad.set(path);
            debugTempDir.set(path.getParent());
            Files.writeString(path, "hello");
        };

        // --- Method invocation ---
        s3Service.exportCompressedToS3(s3Key, fileName, writer);

        // --- Assertions ---
        ArgumentCaptor<Path> compressedDebugFileToUploadCaptor = ArgumentCaptor.forClass(Path.class);
        verify(s3RestService).uploadFile(
            compressedDebugFileToUploadCaptor.capture(),
            eq(s3Key),
            eq(fileName)
        );
        Path compressedDebugFileToUpload = compressedDebugFileToUploadCaptor.getValue();

        assertThat(Files.exists(compressedDebugFileToUpload)).isFalse();
        assertThat(Files.exists(debugFileToUplad.get())).isFalse();
        assertThat(Files.exists(debugTempDir.get())).isFalse();
    }
}
