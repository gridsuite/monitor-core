package org.gridsuite.monitor.worker.server.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3RestServiceTest {
    @Mock
    S3Client s3Client;

    S3RestService s3RestService;

    @TempDir
    Path testDir;

    @BeforeEach
    void setup() {
        s3RestService = new S3RestService(s3Client, "my-bucket");
    }

    @Test
    void testUploadFileToS3() throws Exception {
        Path tempFile = Files.createTempFile(testDir, "test", ".txt");
        Files.writeString(tempFile, "dataToUpload");

        s3RestService.uploadFile(tempFile, "testS3Key", "fileToUploadName");

        ArgumentCaptor<PutObjectRequest> requestCaptor =
            ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(
            requestCaptor.capture(),
            any(RequestBody.class)
        );

        PutObjectRequest request = requestCaptor.getValue();

        assertThat(request.bucket()).isEqualTo("my-bucket");
        assertThat(request.key()).isEqualTo("testS3Key");
        assertThat(request.metadata()).containsEntry("file-name", "fileToUploadName");
    }

    @Test
    void testUpdateFileToS3Error() throws Exception {

        Path fileToUpload = Files.createTempFile(testDir, "test", ".txt");
        Files.writeString(fileToUpload, "dataToUpload");

        doThrow(SdkException.builder().message("sdkError").build())
            .when(s3Client)
            .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThatThrownBy(() ->
            s3RestService.uploadFile(fileToUpload, "key", "file.txt")
        )
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Error occurred while uploading file to S3")
            .hasMessageContaining("sdkError");

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
