package dev.oxydien.simpleModSync.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.exception.JsonValidationException;
import dev.oxydien.simpleModSync.modification.Modification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class SyncSchema {
    public static int CURRENT_VERSION = 3;
    public static int[] SUPPORTED_VERSIONS = new int[] { 2, 3 };

    private final ConcurrentHashMap<Integer, Content> contents = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Modification> modifications = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, SyncStatus> progress = new ConcurrentHashMap<>();

    public SyncWork ParseJson(JsonObject rootObject) {
        if (!rootObject.has("sync_version")) {
            throw new JsonValidationException("sync_version", "Integer");
        }

        int syncVersion = rootObject.get("sync_version").getAsInt();
        if (Arrays.stream(SUPPORTED_VERSIONS).noneMatch((val) -> val == syncVersion)) {
            throw new UnsupportedOperationException(String.format("Invalid sync_version: %d", syncVersion));
        }

        List<Integer> contentIndexesToCheck = new ArrayList<>();

        JsonArray syncArray = new JsonArray();
        if (rootObject.has("sync") && rootObject.get("sync").isJsonArray()) {
            syncArray = rootObject.getAsJsonArray("sync");
        }

        this.progress.clear();

        for (int i = 0; i < syncArray.size(); i++) {
            if (!syncArray.get(i).isJsonObject()) {
                this.progress.put(i, SyncStatus.ofState(SyncStatus.SyncState.INVALID));
                continue;
            }

            this.progress.put(i, new SyncStatus());
            contentIndexesToCheck.add(i);
        }

        List<Integer> modificationIndexesToCheck = new ArrayList<>();

        JsonArray modificationArray = new JsonArray();
        if (rootObject.has("modify") &&  rootObject.get("modify").isJsonArray()) {
            modificationArray = rootObject.getAsJsonArray("modify");
        }

        for (int i = 0; i < modificationArray.size(); i++) {
            if (!modificationArray.get(i).isJsonObject()) {
                continue;
            }

            modificationIndexesToCheck.add(i);
        }

        return new SyncWork(contentIndexesToCheck, modificationIndexesToCheck);
    }

    @FunctionalInterface
    public interface UpdateStatusHandler {
        void updateStatus(SyncStatus status);
    }

    public void withStatus(int index, UpdateStatusHandler handler) {
        if (!this.progress.containsKey(index)) {
            return;
        }

        // Just to be sure :3
        SyncStatus status = this.progress.get(index);
        handler.updateStatus(status);
        this.progress.put(index, status);
    }

    public void setContent(int index, Content content) {
        this.contents.put(index, content);
    }

    public ConcurrentHashMap<Integer, SyncStatus> getProgress() {
        return progress;
    }

    public ConcurrentHashMap<Integer, Content> getContents() {
        return contents;
    }

    public ConcurrentHashMap<Integer, Modification> getModifications() {
        return modifications;
    }

    public record SyncWork(List<Integer> contentsToCheck, List<Integer> modificationsToExecute) {
    }
}