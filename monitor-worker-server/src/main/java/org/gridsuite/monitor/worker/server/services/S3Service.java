/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.gridsuite.monitor.worker.server.clients.S3RestClient;
import org.springframework.stereotype.Service;
import org.springframework.util.function.ThrowingConsumer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Service
public class S3Service {

    private final S3RestClient s3RestClient;
    private final FileService fileService;

    public S3Service(
        S3RestClient s3RestClient,
        FileService fileService) {
        this.s3RestClient = s3RestClient;
        this.fileService = fileService;
    }

    public void compressAndUploadToS3(
        Path source,
        String s3Key,
        String fileName) throws IOException {

        Path compressedFile = fileService.compress(source);

        try {
            s3RestClient.uploadFile(compressedFile, s3Key, fileName);
        } finally {
            fileService.deleteTempFile(compressedFile);
        }
    }
}