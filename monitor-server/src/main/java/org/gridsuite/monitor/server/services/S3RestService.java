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
    private final S3Client s3Client;

    private final String bucketName;

    public S3RestService(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * We did not use downloadDirectory from s3 methods here because it downloads all files on device directly instead of letting us redirect the stream into zip stream
     */
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

    byte[] buildZip(String directoryKey, List<String> filesS3Keys, Function<String, InputStream> s3ObjectFetcher) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (String fileS3Key : filesS3Keys) {
                String zipEntryName = fileS3Key.substring(directoryKey.length() + 1);
                zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
                try (InputStream in = s3ObjectFetcher.apply(fileS3Key)) {
                    in.transferTo(zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
        }

        return outputStream.toByteArray();
    }

    List<String> getFilesKeysInDirectory(String directoryKey) {
        List<String> filesS3Keys = new ArrayList<>();
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(directoryKey)
            .build();
        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(request);
            response.contents().forEach(obj -> filesS3Keys.add(obj.key()));

            request = request.toBuilder()
                .continuationToken(response.nextContinuationToken())
                .build();
        } while (Boolean.TRUE.equals(response.isTruncated())); // S3 pagination, this loop ends if this is the last page

        return filesS3Keys;
    }
}
