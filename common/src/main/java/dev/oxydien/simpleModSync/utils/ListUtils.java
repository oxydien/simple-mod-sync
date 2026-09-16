package dev.oxydien.simpleModSync.utils;

import java.util.List;
import java.util.stream.Collectors;

public class ListUtils {
    public static String peek(List<?> items) {
        if (items == null) {
            return "null";
        }

        return items.stream()
                .limit(5)
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
