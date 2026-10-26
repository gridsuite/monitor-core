package org.gridsuite.monitor.worker.server.services;

import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.ws.commons.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.function.IOConsumer;
import org.gridsuite.monitor.worker.server.core.process.ProcessStepType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    public void withTempFile(String prefix, String suffix, IOConsumer<Path> action) throws IOException {
        FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));

        Path tempDir = Files.createTempDirectory("process-debug", attrs);

        try {
            action.accept(Files.createTempFile(tempDir, prefix, suffix));
        } finally {
            deleteTempDirectory(tempDir);
        }
    }

    void compressFile(Path source, Path destination) throws IOException {
        try (InputStream in = Files.newInputStream(source);
             OutputStream out = new GZIPOutputStream(Files.newOutputStream(destination))) {
            in.transferTo(out);
        }
    }

    void compressDirectory(Path source, Path destination) throws IOException {
        try {
            ZipUtils.zip(source, destination);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public void withDebugDir(ProcessStepType processStepType, Consumer<Path> action) {
        Path debugDir = createDebugDir(processStepType);
        try {
            action.accept(debugDir);
        } finally {
            deleteTempDirectory(debugDir);
        }
    }

    private Path createDebugDir(ProcessStepType processStepType) {
        Path localDir = LocalComputationManager.getDefault().getLocalDir();
        try {
            String debugDirPrefix = buildComputationDirPrefix(processStepType) + "debug_";
            return Files.createTempDirectory(localDir, debugDirPrefix);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(String.format("Error occurred while creating a debug directory inside the local directory %s",
                localDir.toAbsolutePath()), e);
        }
    }

    private String buildComputationDirPrefix(ProcessStepType processStepType) {
        return processStepType.getName().replaceAll("\\s+", "_").toLowerCase() + "_";
    }

    private void deleteTempDirectory(Path directory) {
        try {
            FileUtils.deleteDirectory(directory.toFile());
        } catch (IOException e) {
            LOGGER.error("Error cleaning up temporary directory: {}", directory, e);
        }
    }
}
