package dev.oxydien.simpleModSync.workers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.oxydien.simpleModSync.HandlerRegistry;
import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.config.Config;
import dev.oxydien.simpleModSync.content.SyncSchema;
import dev.oxydien.simpleModSync.content.SyncStatus;
import dev.oxydien.simpleModSync.content.handler.ContentHandler;
import dev.oxydien.simpleModSync.log.Log;
import dev.oxydien.simpleModSync.modification.Modification;
import dev.oxydien.simpleModSync.modification.handler.ModificationHandler;
import dev.oxydien.simpleModSync.utils.DownloadUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SyncWorker implements Runnable {
    public interface SyncWorkerUpdateCallback {
        /// Called whenever the state changes
        void update();
    }

    private final SyncSchema schema;
    private final ConcurrentHashMap<Integer, JsonObject> contentObjects = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, JsonObject> modificationObjects = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<Integer> contentsToCheck = new CopyOnWriteArraySet<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final ExecutorService virtualThreadExecutor;
    private final AtomicReference<SyncStatus> syncStatus = new AtomicReference<>(new SyncStatus());
    private final AtomicReference<List<SyncWorkerUpdateCallback>> updateCallback = new AtomicReference<>();

    public SyncWorker(SyncSchema schema) {
        this.schema = schema;
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public SyncStatus getStatus() {
        return this.syncStatus.get();
    }

    @Override
    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        this.contentsToCheck.clear();

        this.changeStatus(SyncStatus.OfState(SyncStatus.SyncState.RETRIEVING_SCHEMA));

        try {
            // Download the base json
            String url = Config.instance.getSchemaFileUrl();

            if (url.isBlank() || url.length() < 4) {
                this.syncStatus.set(SyncStatus.OfState(SyncStatus.SyncState.UNSYNCED));
                Log.warning("Download is empty or invalid, not syncing");
                return;
            }

            String jsonString = DownloadUtils.downloadString(url);

            this.syncStatus.set(SyncStatus.OfState(SyncStatus.SyncState.PARSING));

            JsonObject rootObject = JsonParser.parseString(jsonString).getAsJsonObject();
            SyncSchema.SyncWork work = schema.ParseJson(rootObject);
            this.contentsToCheck.addAll(work.contentsToCheck());
            this.extractContentObjects(rootObject);
            this.extractModificationObjects(rootObject, work.modificationsToExecute());

            this.changeStatus(SyncStatus.OfState(SyncStatus.SyncState.DOWNLOADING));

            this.processAllContent();

            this.processModifications(work.modificationsToExecute());

            this.finish();
        } catch (IOException e) {
            Log.error("run.SyncWorker.IOException", "Failed to download syncSchema file", e);
            this.changeStatus(SyncStatus.OfError("Failed to download syncSchema file"));
        } catch (URISyntaxException e) {
            Log.error("run.SyncWorker.URISyntaxException", "Invalid sync scheme file URL address", e);
            this.changeStatus(SyncStatus.OfError("Invalid syncScheme URL address"));
        } catch (JsonSyntaxException e) {
            Log.error("run.SyncWorker.JsonSyntaxException", "Invalid json format", e);
            this.changeStatus(SyncStatus.OfError("Invalid json format"));
        } catch (UnsupportedOperationException e) {
            Log.error("run.SyncWorker.UnsupportedOperationException", "Unsupported feature", e);
            this.changeStatus(SyncStatus.OfError("Unsupported feature"));
        } finally {
            isRunning.set(false);
        }
    }

    private void extractContentObjects(JsonObject rootObject) {
        this.contentObjects.clear();

        JsonArray syncArray = new JsonArray();
        if (rootObject.has("sync") && rootObject.get("sync").isJsonArray()) {
            syncArray = rootObject.getAsJsonArray("sync");
        }

        for (int i = 0; i < syncArray.size(); i++) {
            if (syncArray.get(i).isJsonObject() && this.contentsToCheck.contains(i)) {
                this.contentObjects.put(i, syncArray.get(i).getAsJsonObject());
            }
        }
    }

    private void extractModificationObjects(JsonObject rootObject, List<Integer> modificationsToExecute) {
        this.modificationObjects.clear();

        JsonArray modifyArray = new JsonArray();
        if (rootObject.has("modify") && rootObject.get("modify").isJsonArray()) {
            modifyArray = rootObject.getAsJsonArray("modify");
        }

        for (int i = 0; i < modifyArray.size(); i++) {
            if (modifyArray.get(i).isJsonObject() && modificationsToExecute.contains(i)) {
                this.modificationObjects.put(i, modifyArray.get(i).getAsJsonObject());
            } else {
                Log.debug("Modification at index", i, "has invalid structure");
            }
        }
    }

    private void processAllContent() {
        HandlerRegistry registry = SimpleModSync.getInstance().Handlers;
        CountDownLatch latch = new CountDownLatch(contentsToCheck.size());

        for (Integer index : contentsToCheck) {
            this.virtualThreadExecutor.submit(() -> {
                try {
                    this.processContentItem(index, registry);
                } catch (Exception e) {
                    schema.withStatus(index, status -> {
                        status.setErrorMessage("Unexpected error: " + e.getMessage());
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all workers to complete
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processContentItem(int index, HandlerRegistry registry) {
        JsonObject contentObject = contentObjects.get(index);
        if (contentObject == null) {
            schema.withStatus(index, status -> {
                status.setErrorMessage("Content object not found");
            });
            return;
        }

        String type = "mod";
        if (contentObject.has("type") && contentObject.get("type").isJsonPrimitive()
                && contentObject.get("type").getAsJsonPrimitive().isString()) {
            type = contentObject.get("type").getAsString();
        }

        ContentHandler<?> handler = registry.getContentHandler(type);
        if (handler == null) {
            Log.error("Index %d has unknown content handler type: '%s'".formatted(index, type));
            schema.withStatus(index, status -> {
                status.setState(SyncStatus.SyncState.UNSUPPORTED);
            });
            return;
        }

        ContentWorker worker = new ContentWorker(schema);
        worker.Process(contentObject, handler, index);
    }

    private void processModifications(List<Integer> integers) {
        HandlerRegistry registry = SimpleModSync.getInstance().Handlers;

        Log.debug("Running", integers.size(), "modifications");

        for (Integer index : integers) {
            JsonObject modObject = modificationObjects.get(index);
            if (modObject == null) {
                return;
            }

            if (!modObject.has("type") || !modObject.get("type").isJsonPrimitive()) {
                Log.warning("Failed to run modification on index", index, ": No valid type specified");
                return;
            }
            String type = modObject.get("type").getAsString();

            ModificationHandler handler = registry.getModificationHandler(type);
            if (handler == null) {
                Log.warning("Failed to run modification on index", index, ": Unsupported type specified", type);
                return;
            }

            try {
                Modification mod = handler.ParseJson(modObject);
                handler.Execute(mod, SimpleModSync.getInstance().getInstanceDir());
            } catch (Exception e) {
                Log.error("Failed to run modification on index", index, e);
            }
        }
    }

    private void finish() {
        var progresses = this.schema.getProgress();
        boolean anyModified = false;
        boolean anyError = false;

        for (var iterator = progresses.keys().asIterator(); iterator.hasNext();) {
            int key = iterator.next();
            SyncStatus status = progresses.get(key);

            if (status.getState() == SyncStatus.SyncState.ERROR) {
                anyError = true;
            }

            if (status.getState() == SyncStatus.SyncState.MODIFIED) {
                anyModified = true;
            }
        }

        if (anyError) {
            this.changeStatus(SyncStatus.OfState(SyncStatus.SyncState.ERROR));
        } else if (anyModified) {
            this.changeStatus(SyncStatus.OfState(SyncStatus.SyncState.MODIFIED));
        } else {
            this.changeStatus(SyncStatus.OfState(SyncStatus.SyncState.FINISHED));
        }
    }

    private void changeStatus(SyncStatus syncStatus) {
        this.syncStatus.set(syncStatus);
        if (this.updateCallback.get() != null) {
            for (var handler : this.updateCallback.get()) {
                handler.update();
            }
        }
    }

    public void subscribeUpdateCallback(SyncWorkerUpdateCallback callback) {
        if (this.updateCallback.get() == null) {
            this.updateCallback.set(new ArrayList<>());
        }
        this.updateCallback.get().add(callback);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void shutdown() {
        if (this.virtualThreadExecutor != null && !this.virtualThreadExecutor.isShutdown()) {
            this.virtualThreadExecutor.shutdown();
            try {
                if (!this.virtualThreadExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    this.virtualThreadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.virtualThreadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}