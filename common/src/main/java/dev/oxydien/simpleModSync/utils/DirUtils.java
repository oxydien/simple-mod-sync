package dev.oxydien.simpleModSync.utils;

import dev.oxydien.simpleModSync.log.Log;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DirUtils {
    /**
     * Checks if any file in given directory matches the filename. If fullname is false it checks if any file starts with given filename.
     * @param path The directory for the file to be checked
     * @param filename The (partial) filename
     * @param fullname Whether the filename is whole or not
     * @return The path if found or null if not found
     */
    @Nullable
    public static Path dirContains(Path path, String filename, boolean fullname) {
        try (var stream = Files.list(path)) {
            for (var dirFile : stream.toList()) {
                if (fullname) {
                    if (dirFile.getFileName().toString().equals(filename)) {
                        return dirFile;
                    }
                    else {
                        continue;
                    }
                }

                if (dirFile.getFileName().toString().startsWith(filename)) {
                    return dirFile;
                }
            }
        } catch (IOException e) {
            Log.warning("Failed to list contents of " + path, e);
            return null;
        }
        return null;
    }

    /**
     * Returns a list of all file paths under the given directory, recursively.
     * @param startPath The root directory path to start searching from
     * @return List of absolute file paths as strings
     */
    public static List<Path> getFilePaths(Path startPath) {
        List<Path> filePaths = new ArrayList<>();

        if (!startPath.toFile().exists() || !startPath.toFile().isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + startPath);
        }

        collectFilePaths(startPath, filePaths);
        return filePaths;
    }

    private static void collectFilePaths(Path directory, List<Path> filePaths) {
        File[] files = directory.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                filePaths.add(file.toPath());
                if (file.isDirectory()) {
                    collectFilePaths(file.toPath(), filePaths);
                }
            }
        }
    }

    /**
     * Sanitizes a given path by making sure it is a valid path and does not attempt to traverse outside the given base directory.
     * Used so the mod cannot access files outside the base (minecraft) directory.
     *
     * @param baseDir The base directory to work from.
     * @param userProvidedPath The path specified by the user.
     * @return The sanitized path.
     * @throws SecurityException If the path is invalid or attempts to traverse outside the base directory.
     */
    public static Path sanitizePath(Path baseDir, String userProvidedPath) {
        try {
            // Convert paths to canonical form
            Path requestedPath = baseDir.resolve(userProvidedPath).toAbsolutePath().normalize();

            // Check if the requested path starts with the base directory
            if (!requestedPath.startsWith(baseDir)) {
                throw new SecurityException("Path traversal attempt detected");
            }

            return requestedPath;
        } catch (Exception e) {
            throw new SecurityException("Invalid path", e);
        }
    }
}
