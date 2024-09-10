package dev.oxydien.utils;

public class StringUtils {
    public static String removeUnwantedCharacters(String input) {
        return input.replaceAll("[^a-zA-Z0-9.\\-_]", "");
    }
}
