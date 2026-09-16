package dev.oxydien.simpleModSync.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.HandlerRegistry;
import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.exception.JsonValidationException;
import dev.oxydien.simpleModSync.log.Log;
import dev.oxydien.simpleModSync.modification.Modification;
import dev.oxydien.simpleModSync.modification.ModificationTiming;
import dev.oxydien.simpleModSync.modification.handler.ModificationHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
                this.progress.put(i, SyncStatus.OfState(SyncStatus.SyncState.INVALID));
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

    public interface UpdateStatusHandler {
        void UpdateStatus(SyncStatus status);
    }

    public void withStatus(int index, UpdateStatusHandler handler) {
        if (!this.progress.containsKey(index)) {
            return;
        }

        // Just to be sure :3
        SyncStatus status = this.progress.get(index);
        handler.UpdateStatus(status);
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


    public static List<ModificationWork> getModificationsWork(List<Integer> indexesToCheck, Map<Integer, JsonObject> modifications) {
        HandlerRegistry registry = SimpleModSync.getInstance().Handlers;

        List<ModificationWork> out = new ArrayList<>();

        for (Integer index : indexesToCheck) {
            JsonObject modObject = modifications.get(index);
            if (modObject == null) {
                continue;
            }

            if (!modObject.has("type") || !modObject.get("type").isJsonPrimitive()) {
                Log.warning("Failed to parse modification on index", index, ": No valid type specified");
                continue;
            }
            String type = modObject.get("type").getAsString();

            ModificationHandler<?> handler = registry.getModificationHandler(type);
            if (handler == null) {
                Log.warning("Failed to run modification on index", index, ": Unsupported type specified", type);
                continue;
            }

            try {
                Modification mod = handler.ParseJson(modObject);
                out.add(new ModificationWork(index, handler, mod));
            } catch (Exception e) {
                Log.error("Failed to parse modification on index", index, e);
            }
        }

        return out;
    }

    public record SyncWork(List<Integer> contentsToCheck, List<Integer> modificationsToExecute) {
    }

    public record ModificationWork(Integer index, ModificationHandler<?> handler, Modification mod) {
        public ModificationTiming timing() {
            return mod.getTiming();
        }
    }
}