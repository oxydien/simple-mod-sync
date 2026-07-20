package dev.oxydien.simpleModSync.config;

import java.util.List;

public class RepositoryConstraints {

    public static List<String> AllowedDomains() {
        return List.of("forgecdn.net", "curseforge.com", "modrinth.com");
    }
}
