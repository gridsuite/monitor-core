package org.gridsuite.monitor.worker.server.services;

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

@Service
public class S3Service {
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
            debugFile = Files.createTempFile(tempDir, fileName, ".xiidm");
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
                    }
                });
        }
    }
}
