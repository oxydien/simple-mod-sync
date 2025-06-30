package dev.oxydien.workers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.oxydien.data.ConfigData;
import dev.oxydien.data.ContentSyncProgress;
import dev.oxydien.enums.*;
import dev.oxydien.logger.Log;
import dev.oxydien.networking.FileDownloader;
import dev.oxydien.config.Config;
import dev.oxydien.data.ProgressCallback;
import dev.oxydien.data.SyncData;
import dev.oxydien.utils.FileUtils;
import dev.oxydien.utils.PathUtils;
import dev.oxydien.utils.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class ModDownloadWorker implements Runnable {
    /// State where the worker is
    private SyncState state;
    /// Data used for syncing
    @Nullable private SyncData syncData;
    /// Error type if there is one else [SyncErrorType::NONE]
    private SyncErrorType errorType;
    /// Progress of the sync (0 to 100)
    private final AtomicInteger overallProgress;
    /// Callbacks to notify when progress is updated
    public static List<ProgressCallback> callbacks = new CopyOnWriteArrayList<>();
    /// Main worker thread
    private final AtomicReference<Thread> workerThread;
    /// Progress of each content separately
    private final AtomicReference<List<ContentSyncProgress>> contentSyncProgress;
    /// Executor service (waits for completion)
    private CompletionService<Boolean> completionService;
    /// Executor service (runs tasks)
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
        return this.contentSyncProgress.get();
    }

    public ModDownloadWorker() {
        this.state = SyncState.DID_NOT_SYNC;
        this.syncData = null;
        this.errorType = SyncErrorType.NONE;
        this.workerThread = new AtomicReference<>();
        this.overallProgress = new AtomicInteger(0);
        this.contentSyncProgress = new AtomicReference<>(new CopyOnWriteArrayList<>());
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

        // Check if the remote URL is set
        String url = Config.instance.getDownloadUrl();
        if (url.isEmpty()) {
            this.handleError(SyncErrorType.REMOTE_NOT_SET, "Remote URL not set");
            return;
        }

        // Check if the remote URL is set to "-", in which case the synchronization will be disabled
        if (url.equals("-")) {
            this.overallProgress.set(100);
            this.errorType = SyncErrorType.REMOTE_NOT_SET;
            this.setState(SyncState.READY);
            Log.Log.info("bw.run", "Synchronization disabled, returning early");
            return;
        }

        // Download the remote JSON file
        this.updateProgress(2);
        this.setState(SyncState.CHECKING_REMOTE);

        String jsonString;
        try {
            jsonString = FileDownloader.downloadString(url);
        } catch (IOException | URISyntaxException e) {
            this.handleError(SyncErrorType.REMOTE_NOT_FOUND, "Remote URL not found", e);
            return;
        }

        // Parse the JSON file
        this.updateProgress(4);
        this.setState(SyncState.PARSING_REMOTE);
        try {
            this.syncData = this.parseSyncData(jsonString);
        } catch (Exception e) {
            this.handleError(SyncErrorType.PARSING_FAILED, "Failed to parse remote data", e);
            return;
        }

        // Download all the content
        this.updateProgress(10);
        this.setState(SyncState.DOWNLOADING);

        int totalTasks = this.syncData.getContent().size();
        for (SyncData.Content content : this.syncData.getContent()) {
            this.completionService.submit(() -> switch (content.getType()) {
                case "config" -> this.downloadConfig(content);
                case null, default -> this.downloadContent(content);
            });
        }

        // Wait for completion
        boolean changed = false;
        for (int i = 0; i < totalTasks; i++) {
            try {
                Future<Boolean> future = this.completionService.take();
                changed |= future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.Log.error("bw.run.interruptException", "Download process was interrupted for {}",
                        this.syncData.getContent().get(i).getName(), e);
                break;
            } catch (ExecutionException e) {
                Log.Log.error("bw.run.executionException", "Error during parallel download for {}",
                        this.syncData.getContent().get(i).getName(), e);
            }
        }

        // Shutdown executor service and wait for termination
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        this.updateProgress(98);

        // Run modifications
        this.setState(SyncState.MODIFICATIONS);
        for (SyncData.Modification modification : this.syncData.getModify()) {
            this.runModification(modification);
        }

        // Update progress
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
    private boolean downloadContent(SyncData.Content content) {
        // Validate content type
        if (Objects.equals(content.getType(), "config")) {
            Log.Log.warn("bw.downloadContent","DEV ISSUE: config is not supported in this function, skipping {}", content.getName());
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.SUSPICIOUS_CONTENT, null);
            return false;
        }

        // Get working folder
        var workingDirectory = this.getContentValidDirectory(content.getDirectoryOverride(), content.getTypeFolder());
        if (workingDirectory.isEmpty()) {
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.SUSPICIOUS_CONTENT, null);
            return false;
        }

        var contentName = StringUtils.removeUnwantedCharacters(content.getName());
        var contentVersion = StringUtils.removeUnwantedCharacters(content.getVersion());

        // Create full path to the content
        String path = workingDirectory + "/" +
                contentName + "-" +
                contentVersion +
                content.getFileExtension();

        // Check if content already exists
        if (FileDownloader.fileExists(path)) {
            Log.Log.debug("bw.downloadContent","File already exists, skipping {}", content.getName());
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.ALREADY_EXISTS, null);
            return false;
        }

        // Find any older versions
        Path olderVersion = PathUtils.PathExistsFromStartInDir(workingDirectory + "/", contentName);
        if (olderVersion != null) {
            Log.Log.debug("bw.downloadContent", "Found older version of {}, deleting {}", content.getName(), olderVersion.getFileName());
            try {
                Files.delete(olderVersion);
            } catch (IOException e) {
                Log.Log.error("bw.downloadContent.delete.IOException","Failed to delete file", e);
            }
        }

        // Download the content
        Log.Log.debug("bw.downloadContent", "Downloading {} {}", content.getName(), content.getVersion());
        try {
            FileDownloader.downloadFileWithProgress(content.getUrl(), path,
                    (progress) -> this.updateContentProgress(content.getIndex(), progress, ContentSyncOutcome.IN_PROGRESS, null));
        } catch (IOException e) {
            Log.Log.error("bw.downloadContent.write.IOException", "Failed to download file {}", content.getName(), e);
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.DOWNLOAD_INTERRUPTED , e);
            return false;
        } catch (URISyntaxException e) {
            Log.Log.error("bw.downloadContent.write.URISyntaxException", "Failed to download file {}", content.getName(), e);
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.INVALID_URL , e);
            return false;
        }

        this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.SUCCESS, null);
        Log.Log.debug("bw.downloadContent", "Successfully Downloaded {} {} {}", content.getName(), content.getType(), content.getVersion());
        return true;
    }

    /**
     * Downloads a config file from the given url and places it in the valid directory specified by {@link #getContentValidDirectory(String, String)}.
     * <p>
     * This function will check if the config already exists, and if so, will skip the download and return false.
     * If the config is an older version, it will delete the older version and apply the changes from the newer version.
     * If the config is a newer version, it will overwrite the existing config.
     * <p>
     * The progress of the download will be updated in the callback ({@link #updateContentProgress(int, int, ContentSyncOutcome, Exception)}).
     * @param content the (config) content to download
     * @return true if the config was successfully downloaded or updated, false if not
     */
    private boolean downloadConfig(SyncData.Content content) {
        // Validate content type
        if (!Objects.equals(content.getType(), "config")) {
            Log.Log.warn("bw.downloadConfig","DEV ISSUE: {} is not supported in this function, skipping {}", content.getType(), content.getName());
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.SUSPICIOUS_CONTENT, null);
            return false;
        }

        // Get working folder
        var workingDirectory = this.getContentValidDirectory(content.getDirectoryOverride(), content.getTypeFolder());
        if (workingDirectory.isEmpty()) {
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.SUSPICIOUS_CONTENT, null);
            return false;
        }

        var configName = StringUtils.removeUnwantedCharacters(content.getName());
        var configVersion = StringUtils.removeUnwantedCharacters(content.getVersion());

        // Create full path to the config registry (sms_configName-version.json)
        String path = workingDirectory + "/" + "sms_" +
                configName + "-" +
                configVersion +
                ".json";
        var tempZipPath = workingDirectory + "/" + "sms_" +
                configName + "-" +
                configVersion +
                ".archive.zip";

        // Check if config already exists
        if (FileDownloader.fileExists(path)) {
            Log.Log.debug("bw.downloadConfig","Found existing config, skipping {}", content.getName());
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.ALREADY_EXISTS, null);
            return false;
        }
        this.updateContentProgress(content.getIndex(), 10, ContentSyncOutcome.ALREADY_EXISTS, null);

        // Check for older versions
        Path olderVersion = PathUtils.PathExistsFromStartInDir(workingDirectory + "/", configName);
        if (olderVersion != null) {
            // Remove older version's changes
            Log.Log.debug("bw.downloadConfig", "Found older version of {}, deleting changes {}", configName, olderVersion.getFileName());
            boolean allowed = true;
            try {
                var configJsonStr = FileUtils.ReadFile(olderVersion.toString());
                var jsonElement = JsonParser.parseString(configJsonStr);
                var jsonObject = jsonElement.getAsJsonObject();
                var configData = ConfigData.fromJson(jsonObject);

                for (String file : configData.getConfig()) {
                    try {
                        Files.delete(Path.of(file));
                    } catch (IOException e) {
                        Log.Log.warn("bw.downloadConfig.delete.IOException","Failed to delete file, ignoring: ", e);
                    }
                }
            } catch (IOException e) {
                Log.Log.error("bw.downloadConfig.read.IOException", "Failed to read file", e);
                allowed = false;
            }

            if (allowed) {
                try {
                    Files.delete(olderVersion);
                } catch (IOException e) {
                    Log.Log.error("bw.downloadConfig.delete.IOException","Failed to delete file", e);
                }
            }
        }

        // Download the config
        Log.Log.debug("bw.downloadConfig", "Downloading {} {}", content.getName(), content.getVersion());
        try {
            FileDownloader.downloadFileWithProgress(content.getUrl(), tempZipPath,
                    (progress) -> this.updateContentProgress(content.getIndex(), (int) (10 + (progress * 0.8)), ContentSyncOutcome.IN_PROGRESS, null));
            var modifiedFiles = FileUtils.UnZipFile(tempZipPath, workingDirectory);
            var modifiedFilesAsString = modifiedFiles.stream().map(Path::toString).toList();
            var configData = new ConfigData(modifiedFilesAsString);
            var configJsonStr = configData.toJson().toString();
            FileUtils.WriteFile(path, configJsonStr);

            // Remove temp zip file
            Files.delete(Path.of(tempZipPath));
        } catch (IOException e) {
            Log.Log.error("bw.downloadConfig.write.IOException", "Failed to download parse or write file {}", content.getName(), e);
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.DOWNLOAD_INTERRUPTED , e);
            return false;
        } catch (URISyntaxException e) {
            Log.Log.error("bw.downloadConfig.write.URISyntaxException", "Failed to download file {}", content.getName(), e);
            this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.INVALID_URL , e);
            return false;
        }

        this.updateContentProgress(content.getIndex(), 100, ContentSyncOutcome.SUCCESS, null);
        Log.Log.debug("bw.downloadConfig", "Successfully Downloaded {} {} {}", content.getName(), content.getType(), content.getVersion());

        return true;
    }

    private void runModification(SyncData.Modification modification) {
        // Validate input
        if (modification.getType() == SyncModificationType.RENAME && modification.getResult() == null) {
            Log.Log.error("bw.runModification", "Modification type is RENAME but result is null, (result is used for RENAME)");
            return;
        }

        // Get working folder
        var workingDirectory = this.getContentValidDirectory(modification.getPath(), "");
        if (workingDirectory.isEmpty()) {
            Log.Log.debug("bw.runModification", "Suspicious content, skipping {} {}", modification.getType(), modification.getPath());
            return;
        }

        var matchData = FileUtils.GetFilePaths(workingDirectory);
        if (matchData.isEmpty()) {
            Log.Log.debug("bw.runModification", "Ignoring content, no data for {} {}", modification.getType(), modification.getPath());
            return;
        }

        List<String> relativeMatches = new ArrayList<>();
        for (String match : matchData) {
            String relativePath = Path.of(workingDirectory).relativize(Path.of(match)).toString();
            relativeMatches.add(relativePath);
        }

        List<String> matches = new ArrayList<>();
        var pattern = Pattern.compile(modification.getPattern());
        for (var filePath : relativeMatches) {
            var matcher = pattern.matcher(filePath);
            if (matcher.matches()) {
                matches.add(workingDirectory + "/" + filePath);
                Log.Log.debug("bw.runModification", "Found match for {} at {}", modification.getPattern(), (workingDirectory + "/" + filePath));
                break;
            }
        }

        switch (modification.getType()) {
            case REMOVE:
                for (var match : matches) {
                    try {
                        Files.delete(Path.of(match));
                    } catch (IOException e) {
                        Log.Log.error("bw.runModification.delete.IOException","Failed to delete file", e);
                    }
                }
                break;
            case RENAME:
                for (var match : matches) {
                    try {
                        assert modification.getResult() != null;
                        var targetFile = Path.of(match);
                        var parent = targetFile.getParent();
                        Files.move(targetFile, Path.of(parent + "/" + modification.getResult()));
                    } catch (IOException e) {
                        Log.Log.error("bw.runModification.move.IOException","Failed to move file", e);
                    }
                }
                break;
            default:
                Log.Log.error("bw.runModification", "Unknown modification type {}", modification.getType());
        }
        Log.Log.debug("bw.runModification", "Successfully ran modification {} at {}", modification.getType(), workingDirectory);
    }

    /**
     * Updates the progress of the content at the given index in the mod progress list.
     * If the content doesn't exist, a new ContentSyncProgress object is created and added to the list.
     * If the content does exist, its progress is updated.
     * The overall progress is then updated by calling {@link #updateOverallProgress()}.
     *
     * @param contentIndex The index of the content to update.
     * @param progress The new progress of the content.
     * @param outcome The outcome of the content. If the content has finished downloading, this is the outcome.
     * @param e The exception that occurred during the download, if any.
     */
    private void updateContentProgress(int contentIndex, int progress, ContentSyncOutcome outcome, @Nullable Exception e) {
        List<ContentSyncProgress> contentSyncProgress = this.contentSyncProgress.get();
        // Find existing content with the same index
        ContentSyncProgress content = contentSyncProgress.stream()
                .filter(mod -> mod.getIndex() == contentIndex)
                .findFirst().orElse(null);

        if (content == null) {
            // Create new content
            ContentSyncProgress newContent = new ContentSyncProgress(contentIndex, progress);
            if (errorType != null) {
                newContent.setOutcome(outcome, e);
            }
            contentSyncProgress.add(newContent);
        } else {
            // Update existing content's progress
            content.setProgress(progress);
            if (errorType != null) {
                content.setOutcome(outcome, e);
            }
        }
        this.contentSyncProgress.set(contentSyncProgress);
        this.updateOverallProgress();
    }

    /**
     * Calculates the overall progress by summing the progress of all content and dividing it by the total number of content.
     * The overall progress is then set to the calculated value.
     * The state is then set to the current state to trigger a progress update callback.
     */
    private void updateOverallProgress() {
        int totalProgress = (int) ((float) this.contentSyncProgress.get().stream().mapToInt(ContentSyncProgress::getProgress).sum() * 0.95f);
        int overallProgress = totalProgress / this.contentSyncProgress.get().size();
        this.overallProgress.set(overallProgress);
        this.setState(this.state); // Trigger progress update callback
    }

    private void handleError(SyncErrorType errorType, String message) {
        this.handleError(errorType, message, null);
    }

    private void handleError(SyncErrorType errorType, String message, @Nullable Exception e) {
        this.errorType = errorType;
        this.setState(SyncState.ERROR);
        if (e != null) {
            Log.Log.error("bw", "{}", message, e);
        } else {
            Log.Log.error("bw", "{}", message);
        }
    }

    private String getContentValidDirectory(@Nullable String directory, String fallback) {
        String output;
        var baseDir = Config.instance.getDownloadDestination();
        if (directory != null) {
            try {
                output = PathUtils.sanitizePath(baseDir, directory);
            } catch (SecurityException e) {
                Log.Log.error("bw.downloadContent.sanitizePath.SecurityException", "Failed to sanitize path", e);
                return "";
            }
        } else {
            if (fallback.isEmpty()) {
                return "";
            }

            output = baseDir + "/" + fallback;
        }

        if (!PathUtils.PathExists(output)) {
            PathUtils.CreateFolder(output);
        }

        return output;
    }

    private void updateProgress(int progress) {
        this.overallProgress.set(progress);
        this.setState(state); // Trigger progress update callback
    }

    private SyncData parseSyncData(String jsonString) {
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        return SyncData.fromJson(jsonObject);
    }

    private void setState(SyncState state) {
        this.state = state;
        //SimpleModSync.LOGGER.info("[SMS-WORKER] Calling UPDATE callback {}", callbacks);
        for (ProgressCallback progressCallback : callbacks) {
            progressCallback.onProgress(CallbackReason.UPDATE);
        }
    }

    /**
     * Starts the content download worker.
     * This will:
     *  - Clear any stored data
     *  - Initialize the executor service
     *  - Set the state to INITIALIZING
     *  - Start the worker thread
     */
    public void start() {
        Thread thread = new Thread(this);
        this.syncData = null;
        this.contentSyncProgress.set(new CopyOnWriteArrayList<>());
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
