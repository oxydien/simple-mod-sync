package dev.oxydien.simpleModSync.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {
    public static List<Path> ExtractZipFile(Path zipFilePath, Path destinationDirectory) throws IOException {
        List<Path> extractedFiles = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                Path path = destinationDirectory.resolve(zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(path);
                    extractedFiles.add(path.toAbsolutePath());
                } else {
                    Files.createDirectories(path.getParent());
                    Files.copy(zipFile.getInputStream(zipEntry), path, StandardCopyOption.REPLACE_EXISTING);
                    extractedFiles.add(path.toAbsolutePath());
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to extract zip file", e);
        }

        return extractedFiles;
    }
}
