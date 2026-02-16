/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.server.services;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
public class S3RestService {
    public static final String METADATA_FILE_NAME = "file-name";

    private final S3Client s3Client;

    private final String bucketName;

    public S3RestService(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public byte[] downloadDirectoryAsZip(String directoryKey) throws IOException {
        List<String> filesKeys = getFilesKeysInDirectory(directoryKey);

        return buildZip(
            directoryKey,
            filesKeys,
            key -> s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build())
        );
    }

    byte[] buildZip(String directoryKey, List<String> keys, Function<String, InputStream> objectFetcher) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (String key : keys) {
                String entryName = key.substring(directoryKey.length());
                zip.putNextEntry(new ZipEntry(entryName));
                try (InputStream in = objectFetcher.apply(key)) {
                    in.transferTo(zip);
                }
                zip.closeEntry();
            }
        }

        return outputStream.toByteArray();
    }

    List<String> getFilesKeysInDirectory(String directoryKey) {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(directoryKey)
            .build();
        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);
            response.contents().forEach(obj -> keys.add(obj.key()));

            request = request.toBuilder()
                .continuationToken(response.nextContinuationToken())
                .build();
        } while (Boolean.TRUE.equals(response.isTruncated())); // S3 pagination, this loop ends if this is the last page

        return keys;
    }

    /*public byte[] downloadDirectoryAsZip(String directoryKey) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(directoryKey)
                .build();
            ListObjectsV2Response response;
            do {
                response = s3Client.listObjectsV2(request);
                for (S3Object object : response.contents()) {
                    String currentElementKey = object.key();
                    // element key contains parents folder, we make a substring up to "directoryKey"
                    String currentElementName = currentElementKey.substring(directoryKey.length());
                    zipOutputStream.putNextEntry(new ZipEntry(currentElementName));
                    try (ResponseInputStream<GetObjectResponse> s3Stream =
                             s3Client.getObject(GetObjectRequest.builder()
                                 .bucket(bucketName)
                                 .key(currentElementKey)
                                 .build())) {
                        s3Stream.transferTo(zipOutputStream);
                    }
                    zipOutputStream.closeEntry();
                }
                request = request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
            } while (Boolean.TRUE.equals(response.isTruncated())); // S3 pagination, this loop ends if this is the last page
        }

        return outputStream.toByteArray();
    }*/
}
