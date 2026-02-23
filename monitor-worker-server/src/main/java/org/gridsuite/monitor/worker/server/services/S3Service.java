/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.services;

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
import java.util.Comparator;
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

    public void exportCompressedToS3(String s3Key, String fileName, ThrowingConsumer<Path> writer) throws IOException {
        FileAttribute<Set<PosixFilePermission>> attrs =
            PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rwx------"));

        Path tempDir = Files.createTempDirectory("process-debug", attrs);
        Path debugFile = null;
        Path compressedDebugFile = null;

        try {
            debugFile = Files.createTempFile(tempDir, fileName, ".temp");
            compressedDebugFile = Files.createTempFile(tempDir, fileName, ".gz");

            writer.accept(debugFile);

            try (InputStream in = Files.newInputStream(debugFile);
                OutputStream out = new GZIPOutputStream(Files.newOutputStream(compressedDebugFile))) {
                in.transferTo(out);
            }

            s3RestService.uploadFile(compressedDebugFile, s3Key, fileName);
        } finally {
            if (tempDir != null) {
                deleteDirectoryRecursively(tempDir);
            }
        }
    }

    private void deleteDirectoryRecursively(Path directoryPath) throws IOException {
        if (directoryPath == null || !Files.exists(directoryPath)) {
            return;
        }

        try (var walk = Files.walk(directoryPath)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // TODO: should we throw if temp files deletion fails ?
                        LOGGER.warn("Error deleting file {}", path, e);
                    }
                });
        }
    }
}
