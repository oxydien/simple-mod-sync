package dev.oxydien.simpleModSync.modification;

public class Modification {
    private final ModificationType type;
    private final ModificationTiming timing;
    private final String pattern;
    private final String path;

    public Modification(ModificationType type, ModificationTiming timing, String pattern, String path) {
        this.type = type;
        this.timing = timing;
        this.pattern = pattern;
        this.path = path;
    }

    public ModificationType getType() {
        return this.type;
    }

    public ModificationTiming getTiming() {
        return this.timing;
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getPath() {
        return this.path;
    }
}
