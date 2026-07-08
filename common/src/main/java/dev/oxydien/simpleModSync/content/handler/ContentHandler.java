package dev.oxydien.simpleModSync.content.handler;

import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.*;
import dev.oxydien.simpleModSync.exception.JsonValidationException;
import dev.oxydien.simpleModSync.io.FileOperations;
import dev.oxydien.simpleModSync.utils.DirUtils;
import dev.oxydien.simpleModSync.utils.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ContentHandler<T extends Content> {
    public Content ParseJson(JsonObject contentObject, int index) {
        if (!contentObject.has("url")
            && !contentObject.has("uri")) {
            throw new JsonValidationException("url", "String");
        }

        String url = (contentObject.has("url") ? contentObject.get("url") : contentObject.get("uri")).getAsString();

        String name = contentObject.has("name")
                ? contentObject.get("name").getAsString()
                : String.format("UNNAMED_%d", index);

        String typeStr = contentObject.has("type") ? contentObject.get("type").getAsString() : "mod";
        ContentType type = ContentTypeUtils.FromString(typeStr);

        String version = contentObject.has("version") ? contentObject.get("version").getAsString() : "0";

        return new Content(url, type, name, version);
    }

    public boolean CheckExistence(T contentObject) {
        Path dir = this.GetDirectory(SimpleModSync.getInstance().getInstanceDir());

        Path filePath = dir.resolve(this.GetFileName(contentObject));
        return Files.exists(filePath);
    }

    @Nullable
    public Path GetOlderVersion(T contentObject) {
        Path dir = this.GetDirectory(SimpleModSync.getInstance().getInstanceDir());
        String projectName = this.GetProjectName(contentObject);

        return DirUtils.DirContains(dir, projectName, false);
    }

    public String GetFileName(T contentObject) {
        return String.format("%s-%s.%s", StringUtils.sanitize(contentObject.getName()), StringUtils.sanitize(contentObject.getVersion()), this.GetFileExtension());
    }

    public String GetProjectName(T contentObject) {
        return String.format("%s-", StringUtils.sanitize(contentObject.getName()));
    }

    public ContentInfo<T> GetInfo(T contentObject) {
        return new ContentInfo<>(contentObject);
    }

    public boolean NeedsUpdate(T contentObject) {
        return !this.CheckExistence(contentObject);
    }

    public boolean UpdateVersion(T contentObject, FileOperations files, int index) {
        Path dir = this.GetDirectory(SimpleModSync.getInstance().getInstanceDir());
        String fileName = this.GetFileName(contentObject);

        Path outputPath =  dir.resolve(fileName);
        var uri = contentObject.getUri();

        if (uri.trim().isEmpty()) {
            return false;
        }

        files.DownloadFromUri(uri, outputPath, index);
        return true;
    }

    /// 0 -> 1
    public float GetProgress(T contentObject, SyncStatus status) {
        SyncStatus.SyncState state = status.getState();
        if (state == SyncStatus.SyncState.STARTING || state == SyncStatus.SyncState.UNSYNCED) {
            return 0;
        }
        if (state == SyncStatus.SyncState.FINISHED ||  state == SyncStatus.SyncState.MODIFIED) {
            return 1f;
        }
        if (state == SyncStatus.SyncState.PARSING) {
            return 0.03f;
        }

        return status.getDownloadProgress() * 0.9f + 0.1f;
    }

    public abstract String GetFileExtension();

    public abstract Path GetDirectory(Path basePath);

}
