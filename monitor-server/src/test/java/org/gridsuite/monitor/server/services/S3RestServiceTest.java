package org.gridsuite.monitor.server.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3RestServiceTest {
    @InjectMocks
    private S3RestService s3RestService;

    @Mock
    private S3Client s3Client;

    @Test
    void buildZipWithMultipleEntries() throws Exception {
        List<String> keys = List.of(
            "executionId1/debug/file1.txt",
            "executionId1/debug/file2.txt"
        );

        Map<String, String> fakeData = Map.of(
            "executionId1/debug/file1.txt", "content1",
            "executionId1/debug/file2.txt", "content2"
        );

        byte[] zipBytes = s3RestService.buildZip(
            "executionId1",
            keys,
            key -> new ByteArrayInputStream(fakeData.get(key).getBytes())
        );

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry1 = zis.getNextEntry();
            assertEquals("debug/file1.txt", entry1.getName());
            assertEquals("content1", new String(zis.readAllBytes()));

            ZipEntry entry2 = zis.getNextEntry();
            assertEquals("debug/file2.txt", entry2.getName());
            assertEquals("content2", new String(zis.readAllBytes()));
        }
    }

    @Test
    void getFilesFromS3DirectoryWithPagination() {
        S3Object obj1 = S3Object.builder().key("executionId1/debug/file1.txt").build();
        S3Object obj2 = S3Object.builder().key("executionId1/debug/file2.txt").build();

        ListObjectsV2Response firstPage = ListObjectsV2Response.builder()
            .contents(obj1)
            .isTruncated(true)
            .nextContinuationToken("token1")
            .build();

        ListObjectsV2Response secondPage = ListObjectsV2Response.builder()
            .contents(obj2)
            .isTruncated(false)
            .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(firstPage)
            .thenReturn(secondPage);

        List<String> keys = s3RestService.getFilesKeysInDirectory("debug/");

        assertEquals(2, keys.size());
        assertTrue(keys.contains("executionId1/debug/file1.txt"));
        assertTrue(keys.contains("executionId1/debug/file2.txt"));

        verify(s3Client, times(2)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void downloadS3DirectoryContentAsZip() throws Exception {
        S3Object obj = S3Object.builder()
            .key("executionId1/debug/file.txt")
            .build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
            .contents(obj)
            .isTruncated(false)
            .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(response);

        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenReturn(new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(
                    new ByteArrayInputStream("byteContent".getBytes())
                )
            ));

        byte[] zipBytes = s3RestService.downloadDirectoryAsZip("executionId1");

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry = zis.getNextEntry();
            assertEquals("debug/file.txt", entry.getName());
            assertEquals("byteContent", new String(zis.readAllBytes()));
        }
    }
}
