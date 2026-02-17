/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

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

    public void uploadFile(Path filePath, String s3Key, String fileName) throws IOException {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .metadata(Map.of(METADATA_FILE_NAME, fileName))
                .build();
            s3Client.putObject(putRequest, RequestBody.fromFile(filePath));
        } catch (SdkException e) {
            throw new IOException("Error occurred while uploading file to S3: " + e.getMessage(), e);
        }
    }
}
