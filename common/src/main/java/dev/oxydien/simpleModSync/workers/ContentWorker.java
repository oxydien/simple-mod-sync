package dev.oxydien.simpleModSync.workers;

import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.content.Content;
import dev.oxydien.simpleModSync.content.SyncSchema;
import dev.oxydien.simpleModSync.content.SyncStatus;
import dev.oxydien.simpleModSync.content.handler.ContentHandler;
import dev.oxydien.simpleModSync.io.FileOperations;
import dev.oxydien.simpleModSync.log.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContentWorker {
    private final FileOperations files;
    private final SyncSchema schema;

    public ContentWorker(SyncSchema schema) {
        this.schema = schema;
        this.files = new FileOperations(schema);
    }

    public void Process(JsonObject contentObject, ContentHandler handler, int index) {
        long wid = Thread.currentThread().threadId();
        Log.debug("Worker %d is processing index: %d".formatted(wid, index));

        // Parsing content
        this.schema.withStatus(index, (status) -> {
            status.setState(SyncStatus.SyncState.PARSING);
        });

        Content content;
        try {
             content = handler.ParseJson(contentObject, index);
        } catch (Exception e) {
            Log.error("Worker %d encountered an exception while parsing content".formatted(wid), e);
            this.schema.withStatus(index, (status) -> {
                status.setErrorMessage(e.getMessage());
            });
            return;
        }
        this.schema.setContent(index, content);

        Log.debug("Worker %d parsed: %s: %s".formatted(wid, content.getType().toString(), content.getName()));

        // Check directory
        Path contentDir = handler.GetDirectory(SimpleModSync.getInstance().getInstanceDir());
        if (!contentDir.toFile().exists()){
            try {
                Files.createDirectories(contentDir);
            } catch (IOException e) {
                Log.error("Worker %d encountered an exception while checking file system".formatted(wid), e);
                this.schema.withStatus(index, (status) -> {
                    status.setErrorMessage(e.getMessage());
                });
                return;
            }
        }

        // Checking versions
        boolean needsUpdate = handler.NeedsUpdate(content);
        if (!needsUpdate) {
            Log.debug("Worker %d finished index %d, without update".formatted(wid, index));
            this.schema.withStatus(index, (status) -> {
                status.setState(SyncStatus.SyncState.FINISHED);
            });
            return;
        }

        try {
            Path olderVersion = handler.GetOlderVersion(content);
            if (olderVersion != null) {
                Log.info("Worker %d found older version of %s".formatted(wid, content.getName()));

                Files.delete(olderVersion);
            }
        }  catch (Exception e) {
            Log.error("Worker %d encountered an exception while removing older version".formatted(wid), e);
            this.schema.withStatus(index, (status) -> {
                status.setErrorMessage(e.getMessage());
            });
        }

        // Downloading new version
        var downloaded = handler.UpdateVersion(content, this.files , index);

        // Finished
        var exists = handler.CheckExistence(content);
        if ((downloaded && exists) || (!downloaded && exists)) {
            this.schema.withStatus(index, (status) -> {
                status.setState(SyncStatus.SyncState.MODIFIED);
            });
        } else if (!downloaded) {
            this.schema.withStatus(index, (status) -> {
                status.setState(SyncStatus.SyncState.FINISHED);
            });
        }

        if (!downloaded) {
            Log.debug("Worker %d with index %d has removed old content, but has not downloaded a new version (empty url)".formatted(wid, index));
        }

        Log.debug("Worker %d finished index %d".formatted(wid, index));
    }
}
