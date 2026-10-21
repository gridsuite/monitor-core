package org.gridsuite.monitor.worker.server.services;

import com.powsybl.ws.commons.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    public Path createTempFile(String prefix, String suffix) throws IOException {
        FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));

        Path tempDir = Files.createTempDirectory("process-debug", attrs);

        try {
            return Files.createTempFile(tempDir, prefix, suffix);
        } catch (IOException e) {
            deleteTempFile(tempDir);
            throw e;
        }
    }

    public Path compressFile(Path source) throws IOException {
        Path compressedFile = createTempFile(source.getFileName().toString(), ".gz");

        try {
            try (InputStream in = Files.newInputStream(source);
                 OutputStream out = new GZIPOutputStream(Files.newOutputStream(compressedFile))) {
                in.transferTo(out);
            }

            return compressedFile;
        } catch (IOException e) {
            deleteTempFile(compressedFile);
            throw e;
        }
    }

    public Path compressDirectory(Path source) throws IOException {
        Path compressedFile = createTempFile(source.getFileName().toString(), ".zip");

        ZipUtils.zip(source, compressedFile);
        return compressedFile;
    }

    public void deleteTempFile(Path path) {
        if (path == null) {
            return;
        }

        try {
            Path directory = Files.isDirectory(path) ? path : path.getParent();

            FileUtils.deleteDirectory(directory.toFile());
        } catch (IOException e) {
            LOGGER.error("Error cleaning up temporary file: {}", path, e);
        }
    }
}
