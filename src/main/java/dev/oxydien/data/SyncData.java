package dev.oxydien.data;

import java.util.List;

public class SyncData {
    private int syncVersion;
    private List<Content> content;

    public SyncData(int syncVersion, List<Content> content) {
        this.syncVersion = syncVersion;
        this.content = content;
    }

    public int getSyncVersion() {
        return syncVersion;
    }

    public List<Content> getContent() {
        return content;
    }

    public static class Content {
        private String url;
        private String version;
        private String modName;

        public Content(String url, String version, String fileName) {
            this.url = url;
            this.version = version;
            this.modName = fileName;
        }

        public String getUrl() {
            return url;
        }

        public String getModName() {
            return modName;
        }

        public String getVersion() {
            return version;
        }
    }
}
