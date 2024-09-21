package dev.oxydien.data;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SyncData {
    private final int syncVersion;
    private final List<Content> content;

    public SyncData(int syncVersion, List<Content> content) {
        this.syncVersion = syncVersion;
        this.content = content;
    }

    public int getSyncVersion() {
        return syncVersion;
    }

    public List<Content> getContent() {
        return Collections.unmodifiableList(this.content);
    }

    public static class Content {
        private final int index;
        private final String url;
        private final String version;
        private final String modName;
        @Nullable private final String type;

        public Content(int index, String url, String version, String fileName, @Nullable String type) {
            this.index = index;
            this.url = url;
            this.version = version;
            this.modName = fileName;
            this.type = type;
        }

        public int getIndex() {
            return this.index;
        }

        public String getUrl() {
            return this.url;
        }

        public String getModName() {
            return this.modName;
        }

        public String getVersion() {
            return this.version;
        }

        @Nullable
        public String getType() {
            return this.type;
        }

        public String getTypeFolder() {
            return switch (this.getType()) {
                case "resourcepack" -> "resourcepacks";
                case "shader" -> "shaderpacks";
                case "datapack" -> "datapacks";
                case null, default -> "mods";
            };
        }

        public String getFileExtension() {
            return switch (this.getType()) {
                case "resourcepack", "shaderpack", "datapack" -> ".zip";
                case null, default -> ".jar";
            };
        }
    }
}
