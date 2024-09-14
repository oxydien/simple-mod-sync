package dev.oxydien.utils;

import dev.oxydien.SimpleModSync;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathUtils {

    @Nullable
    public static Path PathExistsFromStartInDir(String dirPath, String search) {
        Path dir = Path.of(dirPath);
        if (Files.isDirectory(dir)) {
            try (var stream = Files.list(dir)) {
                for (var p : stream.toList()) {
                    if (p.getFileName().toString().startsWith(search)) {
                        return p;
                    }
                }
            } catch (IOException e) {
                SimpleModSync.LOGGER.error("Error while searching for file in directory", e);
            }
        }
        return null;
    }

    public static boolean PathExists(String path) {
        return Files.exists(Path.of(path));
    }

    public static void CreateFolder(String path) {
        try {
            Files.createDirectories(Path.of(path));
        } catch (IOException e) {
            SimpleModSync.LOGGER.error("Error while creating folder", e);
        }
    }
}
