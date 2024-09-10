package dev.oxydien.workers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.oxydien.Networking.FileDownloader;
import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.data.ProgressCallback;
import dev.oxydien.data.SyncData;
import dev.oxydien.enums.CallbackReason;
import dev.oxydien.enums.SyncErrorType;
import dev.oxydien.enums.SyncState;
import dev.oxydien.utils.PathUtils;
import dev.oxydien.utils.StringUtils;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ModDownloadWorker implements Runnable {
    private SyncState state;
    private SyncErrorType errorType;
    private int progress;
    public static List<ProgressCallback> callbacks = new ArrayList<>();
    private final AtomicReference<Thread> workerThread;

    public SyncState GetState() {
        return this.state;
    }

    public SyncErrorType GetErrorType() {
        return this.errorType;
    }

    public int GetProgress() {
        return this.progress;
    }

    public ModDownloadWorker() {
        this.state = SyncState.INITIALIZING;
        this.errorType = SyncErrorType.NONE;
        this.workerThread = new AtomicReference<>();
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

    @Override
    public void run() {
        workerThread.set(Thread.currentThread());
        SimpleModSync.LOGGER.info("[SMS-WORKER] Mod download worker started");

        String url = Config.instance.getDownloadUrl();
        if (url.isEmpty()) {
            this.errorType = SyncErrorType.REMOTE_NOT_SET;
            this.setState(SyncState.ERROR);
            SimpleModSync.LOGGER.info("[SMS-WORKER] Remote URL not set");
            return;
        }

        if (url.equals("-")) {
            this.progress = -1;
            this.setState(SyncState.READY);
            this.errorType = SyncErrorType.REMOTE_NOT_SET;
            SimpleModSync.LOGGER.info("[SMS-WORKER] Synchronization disabled, returning early");
            return;
        }

        this.progress = 2;
        this.setState(SyncState.CHECKING_REMOTE);
        String jsonString;
        try {
            jsonString = FileDownloader.downloadString(url);
        } catch (IOException e) {
            this.errorType = SyncErrorType.REMOTE_NOT_FOUND;
            this.setState(SyncState.ERROR);
            SimpleModSync.LOGGER.error("[SMS-WORKER] Remote URL not found", e);
            return;
        }

        this.progress = 4;
        this.setState(SyncState.PARSING_REMOTE);
        SyncData data;
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            int syncVersion = JsonHelper.getInt(jsonObject, "sync_version");
            List<SyncData.Content> contentList = new ArrayList<>();

            for (JsonElement contentElement : JsonHelper.getArray(jsonObject, "content")) {
                JsonObject contentObject = contentElement.getAsJsonObject();
                String modUrl = JsonHelper.getString(contentObject, "url");
                String version = JsonHelper.getString(contentObject, "version");
                String modName = JsonHelper.getString(contentObject, "mod_name");

                SyncData.Content content = new SyncData.Content(modUrl, version, modName);
                contentList.add(content);
            }

            data = new SyncData(syncVersion, contentList);
        } catch (Exception e) {
            this.errorType = SyncErrorType.PARSING_FAILED;
            this.setState(SyncState.ERROR);
            SimpleModSync.LOGGER.error("[SMS-WORKER] Failed to parse remote data", e);
            return;
        }

        this.progress = 10;
        this.setState(SyncState.DOWNLOADING);
        boolean changed = false;
        int index = 0;
        int total = data.getContent().size();
        for (SyncData.Content content : data.getContent()) {
            index++;
            String path = Config.instance.getDownloadDestination() + "/" +
                    StringUtils.removeUnwantedCharacters(content.getModName()) + "-" +
                    StringUtils.removeUnwantedCharacters(content.getVersion()) +
                    ".jar";
            this.setState(SyncState.DOWNLOADING);

            if (FileDownloader.fileExists(path)) {
                SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) File already exists, skipping {}", index, total, content.getModName());
                this.progress = 10 + (int) ((index * 90.0) / total);
                continue;
            }

            Path olderVersion = PathUtils.PathExistsFromStartInDir(Config.instance.getDownloadDestination(), content.getModName());
            if (olderVersion != null) {
                SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) Found older version, deleting {}", index, total, olderVersion.getFileName());
                try {
                    Files.delete(olderVersion);
                } catch (IOException e) {
                    SimpleModSync.LOGGER.error("[SMS-WORKER] Failed to delete file", e);
                }
            }

            SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) Downloading {} {}", index, total, content.getModName(), content.getVersion());
            try {
                FileDownloader.downloadFile(content.getUrl(), path);
            } catch (IOException e) {
                this.errorType = SyncErrorType.DOWNLOAD_FAILED;
                this.setState(SyncState.ERROR);
                SimpleModSync.LOGGER.error("[SMS-WORKER] Failed to download file", e);
                continue;
            }

            changed = true;
            this.progress = 10 + (int) ((index * 90.0) / total);
            SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) Downloaded {} {}", index, total, content.getModName(), content.getVersion());
        }

        this.progress = 100;
        if (changed) {
            this.setState(SyncState.NEEDS_RESTART);
        } else {
            this.setState(SyncState.READY);
        }

        SimpleModSync.LOGGER.info("[SMS-WORKER] Synchronization finished");
    }

    private void setState(SyncState state) {
        this.state = state;
        //SimpleModSync.LOGGER.info("[SMS-WORKER] Calling UPDATE callback {}", callbacks);
        for (ProgressCallback progressCallback : callbacks) {
            progressCallback.simple_mod_sync$onProgressUpdate(CallbackReason.UPDATE);
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        Thread thread = workerThread.get();
        if (thread != null) {
            thread.interrupt();
        }
    }
}
