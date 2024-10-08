package dev.oxydien.workers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.oxydien.data.ContentSyncProgress;
import dev.oxydien.enums.ContentSyncOutcome;
import dev.oxydien.logger.Log;
import dev.oxydien.networking.FileDownloader;
import dev.oxydien.config.Config;
import dev.oxydien.data.ProgressCallback;
import dev.oxydien.data.SyncData;
import dev.oxydien.enums.CallbackReason;
import dev.oxydien.enums.SyncErrorType;
import dev.oxydien.enums.SyncState;
import dev.oxydien.utils.PathUtils;
import dev.oxydien.utils.StringUtils;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ModDownloadWorker implements Runnable {
    private SyncState state;
    @Nullable private SyncData syncData;
    private SyncErrorType errorType;
    private final AtomicInteger overallProgress;
    public static List<ProgressCallback> callbacks = new CopyOnWriteArrayList<>();
    private final AtomicReference<Thread> workerThread;
    private final AtomicReference<List<ContentSyncProgress>> modProgress;
    private CompletionService<Boolean> completionService;
    private ExecutorService executorService;

    public SyncState GetState() {
        return this.state;
    }

    public SyncErrorType GetErrorType() {
        return this.errorType;
    }

    public int GetProgress() {
        return this.overallProgress.get();
    }

    public SyncData GetSyncData() {
        return this.syncData;
    }

    public List<ContentSyncProgress> GetModProgress() {
        return this.modProgress.get();
    }

    public ModDownloadWorker() {
        this.state = SyncState.DID_NOT_SYNC;
        this.syncData = null;
        this.errorType = SyncErrorType.NONE;
        this.workerThread = new AtomicReference<>();
        this.overallProgress = new AtomicInteger(0);
        this.modProgress = new AtomicReference<>(new CopyOnWriteArrayList<>());
        this.executorService = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 4));
        this.completionService = new ExecutorCompletionService<>(this.executorService);
    }

    public void subscribe(ProgressCallback callback) {
        if (callback != null && !callbacks.contains(callback)) {
            callbacks.add(callback);
            //SimpleModSync.LOGGER.info("[SMS-WORKER] Added callback {} {}", callback, callbacks);
        }
    }

    public void unsubscribe(ProgressCallback callback) {
        if (callback != null) {
            callbacks.remove(callback);
        }
    }

    /**
     * Starts the mod download worker. This will:
     *  - Check if the remote URL is set, and if not, set an error and return
     *  - Check if the remote URL is set to "-", in which case the synchronization will be disabled
     *  - Download the remote JSON file
     *  - Parse the JSON file
     *  - Download all mods listed in the JSON file
     *  - Set the state to READY or NEEDS_RESTART depending on whether any mods were downloaded
     */
    @Override
    public void run() {
        workerThread.set(Thread.currentThread());
        Log.Log.info("bw.run", "Mod download worker started");

        String url = Config.instance.getDownloadUrl();
        if (url.isEmpty()) {
            this.handleError(SyncErrorType.REMOTE_NOT_SET, "Remote URL not set");
            return;
        }

        if (url.equals("-")) {
            this.overallProgress.set(100);
            this.errorType = SyncErrorType.REMOTE_NOT_SET;
            this.setState(SyncState.READY);
            Log.Log.info("bw.run", "Synchronization disabled, returning early");
            return;
        }

        this.updateProgress(2);
        this.setState(SyncState.CHECKING_REMOTE);
        String jsonString;
        try {
            jsonString = FileDownloader.downloadString(url);
        } catch (IOException | URISyntaxException e) {
            this.handleError(SyncErrorType.REMOTE_NOT_FOUND, "Remote URL not found", e);
            return;
        }

        this.updateProgress(4);
        this.setState(SyncState.PARSING_REMOTE);
        try {
            this.syncData = this.parseSyncData(jsonString);
        } catch (Exception e) {
            this.handleError(SyncErrorType.PARSING_FAILED, "Failed to parse remote data", e);
            return;
        }

        this.updateProgress(10);
        this.setState(SyncState.DOWNLOADING);

        int totalTasks = this.syncData.getContent().size();
        for (SyncData.Content content : this.syncData.getContent()) {
            this.completionService.submit(() -> this.downloadMod(content));
        }

        boolean changed = false;
        for (int i = 0; i < totalTasks; i++) {
            try {
                Future<Boolean> future = this.completionService.take();
                changed |= future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.Log.error("bw.run.interruptException", "Download process was interrupted for {}",
                        this.syncData.getContent().get(i).getModName(), e);
                break;
            } catch (ExecutionException e) {
                Log.Log.error("bw.run.executionException", "Error during parallel download for {}",
                        this.syncData.getContent().get(i).getModName(), e);
            }
        }

        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        this.updateProgress(100);
        if (changed) {
            this.setState(SyncState.NEEDS_RESTART);
        } else {
            this.setState(SyncState.READY);
        }

        Log.Log.info("bw.run", "Synchronization finished");
    }

    /**
     * Downloads a mod from the given content URL to the download destination.
     *
     * <p>If the file already exists, it will be skipped. If an older version of the mod exists, it will be deleted.
     * This method will catch any exceptions that occur during the download process and return false.
     *
     * @param content The content to download.
     * @return Whether the mod was downloaded successfully.
     */
    private boolean downloadMod(SyncData.Content content) {

        var folder = Config.instance.getDownloadDestination() + "/" + content.getTypeFolder();

        if (!PathUtils.PathExists(folder)) {
            PathUtils.CreateFolder(folder);
        }

        var modName = StringUtils.removeUnwantedCharacters(content.getModName());
        var modVersion = StringUtils.removeUnwantedCharacters(content.getVersion());

        String path = folder + "/" +
                modName + "-" +
                modVersion +
                content.getFileExtension();

        if (FileDownloader.fileExists(path)) {
            Log.Log.debug("bw.downloadMod","File already exists, skipping {}", content.getModName());
            this.updateModProgress(content.getIndex(), 100, ContentSyncOutcome.ALREADY_EXISTS, null);
            return false;
        }

        Path olderVersion = PathUtils.PathExistsFromStartInDir(folder + "/", modName);
        if (olderVersion != null) {
            Log.Log.debug("bw.downloadMod", "Found older version of {}, deleting {}", content.getModName(), olderVersion.getFileName());
            try {
                Files.delete(olderVersion);
            } catch (IOException e) {
                Log.Log.error("bw.downloadMod.delete.IOException","Failed to delete file", e);
            }
        }

        Log.Log.debug("bw.downloadMod", "Downloading {} {}", content.getModName(), content.getVersion());
        try {
            FileDownloader.downloadFileWithProgress(content.getUrl(), path,
                    (progress) -> this.updateModProgress(content.getIndex(), progress, ContentSyncOutcome.IN_PROGRESS, null));
        } catch (IOException e) {
            Log.Log.error("bw.downloadMod.write.IOException", "Failed to download file {}", content.getModName(), e);
            this.updateModProgress(content.getIndex(), 100, ContentSyncOutcome.DOWNLOAD_INTERRUPTED , e);
            return false;
        } catch (URISyntaxException e) {
            Log.Log.error("bw.downloadMod.write.URISyntaxException", "Failed to download file {}", content.getModName(), e);
            this.updateModProgress(content.getIndex(), 100, ContentSyncOutcome.INVALID_URL , e);
            return false;
        }

        this.updateModProgress(content.getIndex(), 100, ContentSyncOutcome.SUCCESS, null);
        Log.Log.debug("bw.downloadMod", "Successfully Downloaded {} {} {}", content.getModName(), content.getType(), content.getVersion());
        return true;
    }

    private void updateModProgress(int modIndex, int progress, ContentSyncOutcome outcome, @Nullable Exception e) {
        List<ContentSyncProgress> modProgress = this.modProgress.get();
        ContentSyncProgress content = modProgress.stream().filter(mod -> mod.getIndex() == modIndex).findFirst().orElse(null);
        if (content == null) {
            ContentSyncProgress newContent = new ContentSyncProgress(modIndex, progress);
            if (errorType != null) {
                newContent.setOutcome(outcome, e);
            }
            modProgress.add(newContent);
        } else {
            content.setProgress(progress);
            if (errorType != null) {
                content.setOutcome(outcome, e);
            }
        }
        this.modProgress.set(modProgress);
        this.updateOverallProgress();
    }

    private void updateOverallProgress() {
        int totalProgress = modProgress.get().stream().mapToInt(ContentSyncProgress::getProgress).sum();
        int overallProgress = totalProgress / modProgress.get().size();
        this.overallProgress.set(overallProgress);
        this.setState(this.state);
    }

    private void handleError(SyncErrorType errorType, String message) {
        this.handleError(errorType, message, null);
    }

    private void handleError(SyncErrorType errorType, String message, Exception e) {
        this.errorType = errorType;
        this.setState(SyncState.ERROR);
        if (e != null) {
            Log.Log.error("bw", "{}", message, e);
        } else {
            Log.Log.error("bw", "{}", message);
        }
    }

    private void updateProgress(int progress) {
        this.overallProgress.set(progress);
        this.setState(state); // Trigger progress update callback
    }

    private SyncData parseSyncData(String jsonString) {
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        int syncVersion = JsonHelper.getInt(jsonObject, "sync_version");
        List<SyncData.Content> contentList = new ArrayList<>();

        int index = 0;
        for (JsonElement contentElement : JsonHelper.getArray(jsonObject, "content")) {
            JsonObject contentObject = contentElement.getAsJsonObject();
            String modUrl = JsonHelper.getString(contentObject, "url");
            String version = JsonHelper.getString(contentObject, "version");
            String modName = JsonHelper.getString(contentObject, "mod_name");
            String type = contentObject.get("type") != null ? contentObject.get("type").getAsString() : null;

            SyncData.Content content = new SyncData.Content(index, modUrl, version, modName, type);
            contentList.add(content);
            index++;
        }

        return new SyncData(syncVersion, contentList);
    }

    private void setState(SyncState state) {
        this.state = state;
        //SimpleModSync.LOGGER.info("[SMS-WORKER] Calling UPDATE callback {}", callbacks);
        for (ProgressCallback progressCallback : callbacks) {
            progressCallback.onProgress(CallbackReason.UPDATE);
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        this.syncData = null;
        this.modProgress.set(new CopyOnWriteArrayList<>());
        this.overallProgress.set(0);
        this.errorType = null;
        this.executorService = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 4));
        this.completionService = new ExecutorCompletionService<>(this.executorService);
        this.setState(SyncState.INITIALIZING);
        this.workerThread.set(thread);
        thread.start();
    }

    public void stop() {
        Thread thread = workerThread.get();
        if (thread != null) {
            thread.interrupt();
        }
    }
}
