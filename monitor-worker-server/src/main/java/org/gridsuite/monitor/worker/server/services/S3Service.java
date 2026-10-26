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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Service
public class S3Service {

    private final S3RestClient s3RestClient;
    private final FileService fileService;

    S3Service(
        S3RestClient s3RestClient,
        FileService fileService) {
        this.s3RestClient = s3RestClient;
        this.fileService = fileService;
    }

    public void exportCompressedToS3(String s3Key, String fileNamePrefix, String fileNameSuffix, ThrowingConsumer<Path> writer) throws IOException {
        fileService.withTempFile(fileNamePrefix, fileNameSuffix, tempFile -> {
            writer.accept(tempFile);
            compressAndUploadToS3(tempFile, s3Key, fileNamePrefix + fileNameSuffix + ".gz");
        });
    }

    public void compressAndUploadToS3(
        Path source,
        String s3Key,
        String uploadedFileName) throws IOException {

        boolean directory = Files.isDirectory(source);
        fileService.withTempFile(source.getFileName().toString(), directory ? ".zip" : ".gz", compressedFile -> {
            if (directory) {
                fileService.compressDirectory(source, compressedFile);
            } else {
                fileService.compressFile(source, compressedFile);
            }
            s3RestClient.uploadFile(compressedFile, s3Key, uploadedFileName);
        });
    }
}
