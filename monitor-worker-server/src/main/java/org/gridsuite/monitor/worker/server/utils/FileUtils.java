package org.gridsuite.monitor.worker.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    public static Path createZipFile(Path tempDir, String fileOrNetworkName, Set<String> fileNames) throws IOException {
        Path zipFile = tempDir.resolve(fileOrNetworkName + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (String fileName : fileNames) {
                Path sourceFile = tempDir.resolve(fileName);
                zos.putNextEntry(new ZipEntry(fileName));
                try (InputStream is = Files.newInputStream(sourceFile)) {
                    is.transferTo(zos);
                }
                zos.closeEntry();
            }
        }
        return zipFile;
    }
}
