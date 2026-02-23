/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.function.ThrowingConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * @author Kevin Le Saulnier <kevin.le-saulnier at rte-france.com>
 */
@Service
public class S3Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Service.class);
    private final S3RestService s3RestService;

    public S3Service(S3RestService s3RestService) {
        this.s3RestService = s3RestService;
    }

    public void exportCompressedToS3(String s3Key, String fileNamePrefix, String fileNameSuffix, ThrowingConsumer<Path> writer) throws IOException {
        FileAttribute<Set<PosixFilePermission>> attrs =
            PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rwx------"));

        Path tempDir = Files.createTempDirectory("process-debug", attrs);

        try {
            Path debugFile = Files.createTempFile(tempDir, fileNamePrefix, fileNameSuffix);
            Path compressedDebugFile = Files.createTempFile(tempDir, String.join("", fileNamePrefix, fileNameSuffix), ".gz");

            writer.accept(debugFile);

            try (InputStream in = Files.newInputStream(debugFile);
                OutputStream out = new GZIPOutputStream(Files.newOutputStream(compressedDebugFile))) {
                in.transferTo(out);
            }

            s3RestService.uploadFile(compressedDebugFile, s3Key, String.join("", fileNamePrefix, fileNameSuffix, ".gz"));
        } finally {
            try {
                if (Files.exists(tempDir)) {
                    FileUtils.deleteDirectory(tempDir.toFile());
                }
            } catch (IOException e) {
                LOGGER.error("Error cleaning up temporary debug directory: {}", tempDir, e);
            }
        }
    }
}
