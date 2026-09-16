package dev.oxydien.simpleModSync.utils;

public class StringUtils {
    public static String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9.\\-_+]", "");
    }

    public static String sanitizeDirectory(String input) {
        return input.replaceAll("[^a-zA-Z0-9. \\-/_\\\\]", "");
    }
}
