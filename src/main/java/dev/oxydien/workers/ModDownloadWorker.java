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

public class ModDownloadWorker implements Runnable {
    private SyncState state;
    private SyncErrorType errorType;
    private int progress;
    private List<ProgressCallback> progressCallback;

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
        this.progressCallback = new ArrayList<>();
    }

    public void Subscribe(ProgressCallback callback) {
        if (!this.progressCallback.contains(callback)) {
            this.progressCallback.add(callback);
            SimpleModSync.LOGGER.info("[SMS-WORKER] Added callback {}", callback);
        }
    }

    public void Unsubscribe(ProgressCallback callback) {
        this.progressCallback.remove(callback);
        SimpleModSync.LOGGER.info("[SMS-WORKER] Removed callback {}", callback);
    }

    @Override
    public void run() {
        SimpleModSync.LOGGER.info("[SMS-WORKER] Mod download worker started");

        String url = Config.instance.getDownloadUrl();
        if (url.isEmpty()) {
            this.errorType = SyncErrorType.REMOTE_NOT_SET;
            this.SetState(SyncState.ERROR);
            SimpleModSync.LOGGER.info("[SMS-WORKER] Remote URL not set");
            return;
        }

        // If url is disabled
        if (url.equals("-")) {
            this.progress = -1;
            this.SetState(SyncState.READY);
            this.errorType = SyncErrorType.REMOTE_NOT_SET;
            SimpleModSync.LOGGER.info("[SMS-WORKER] Synchronization disabled, returning early");
            return;
        }

        this.progress = 2;
        this.SetState(SyncState.CHECKING_REMOTE);
        String jsonString = "";
        try {
            jsonString = FileDownloader.downloadString(url);
        } catch (IOException e) {
            this.errorType = SyncErrorType.REMOTE_NOT_FOUND;
            this.SetState(SyncState.ERROR);
            SimpleModSync.LOGGER.error("[SMS-WORKER] Remote URL not found", e);
            return;
        }

        this.progress = 4;
        this.SetState(SyncState.PARSING_REMOTE);
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
            this.SetState(SyncState.ERROR);
            SimpleModSync.LOGGER.error("[SMS-WORKER] Failed to parse remote data", e);
            return;
        }

        this.progress = 10;
        this.SetState(SyncState.DOWNLOADING);
        boolean changed = false;
        int index = 0;
        int total = data.getContent().size();
        for (SyncData.Content content : data.getContent()) {
            index++;
            String path = Config.instance.getDownloadDestination() + "/" +
                    StringUtils.removeUnwantedCharacters(content.getModName()) + "-" +
                    StringUtils.removeUnwantedCharacters(content.getVersion()) +
                    ".jar";
            this.SetState(SyncState.DOWNLOADING);

            // If same file exists, skip
            if (FileDownloader.fileExists(path)) {
                SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) File already exists, skipping {}", index, total, content.getModName());
                this.progress = 10 + (int) ((index * 90.0) / total);
                continue;
            }

            // If older version exists, delete
            Path olderVersion = PathUtils.PathExistsFromStartInDir(Config.instance.getDownloadDestination(), content.getModName());
            if (olderVersion != null) {
                SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) Found older version, deleting {}", index, total, olderVersion.getFileName());
                try {
                    Files.delete(olderVersion);
                } catch (IOException e) {
                    SimpleModSync.LOGGER.error("[SMS-WORKER] Failed to delete file", e);
                }
            }

            // Download new version
            SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) Downloading {} {}", index, total, content.getModName(), content.getVersion());
            try {
                FileDownloader.downloadFile(content.getUrl(), path);
            } catch (IOException e) {
                this.errorType = SyncErrorType.DOWNLOAD_FAILED;
                this.SetState(SyncState.ERROR);
                SimpleModSync.LOGGER.error("[SMS-WORKER] Failed to download file", e);
                continue;
            }

            changed = true;
            this.progress = 10 + (int) ((index * 90.0) / total);
            SimpleModSync.LOGGER.info("[SMS-WORKER] ({}/{}) Downloaded {} {}", index, total, content.getModName(), content.getVersion());
        }

        this.progress = 100;
        if (changed) {
            this.SetState(SyncState.NEEDS_RESTART);
        } else {
            this.SetState(SyncState.READY);
        }

        SimpleModSync.LOGGER.info("[SMS-WORKER] Synchronization finished");
    }

    private void SetState(SyncState state) {
        this.state = state;

        //SimpleModSync.LOGGER.info("[SMS-WORKER] Calling UPDATE callback {}", this.progressCallback);
        for (ProgressCallback progressCallback : this.progressCallback) {
            progressCallback.simple_mod_sync$onProgressUpdate(CallbackReason.UPDATE);
        }
    }
}
