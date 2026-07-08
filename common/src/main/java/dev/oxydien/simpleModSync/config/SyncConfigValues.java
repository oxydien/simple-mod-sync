package dev.oxydien.simpleModSync.config;

import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.log.Log;
import org.jetbrains.annotations.NotNull;

public class SyncConfigValues implements IConfigCodec {
    private static final String SCHEMA_FILE_URL_KEY = "schema_file_url";
    private static final String AUTO_SYNC_KEY = "sync_on_startup";

    private static final String COMPAT_DOWNLOAD_URL_KEY = "download_url";
    private static final String COMPAT_AUTO_SYNC_KEY = "auto_download";


    private String syncSchemaFileUrl = "";
    private boolean syncOnStartup = true;


    public static @NotNull SyncConfigValues createDefault() {
        return new SyncConfigValues();
    }

    @Override
    public void writeTo(JsonObject parent) {
        parent.addProperty(SCHEMA_FILE_URL_KEY, this.syncSchemaFileUrl);
        parent.addProperty(AUTO_SYNC_KEY, this.syncOnStartup);
    }

    @Override
    public void readFrom(JsonObject parent) {

        var autoSync = this.syncOnStartup;
        var autoSyncAny = parent.get(AUTO_SYNC_KEY);
        if (autoSyncAny != null && autoSyncAny.isJsonPrimitive()) {
            var autoSyncPrimitive = autoSyncAny.getAsJsonPrimitive();
            autoSync = autoSyncPrimitive.isBoolean() && autoSyncPrimitive.getAsBoolean();
        } else
            Log.debug(SyncConfigValues.class, AUTO_SYNC_KEY + " not found or invalid.");

        var syncSchemaFileUrl = this.syncSchemaFileUrl;
        var fileUrlAny = parent.get(SCHEMA_FILE_URL_KEY);
        if (fileUrlAny != null && fileUrlAny.isJsonPrimitive()) {
            var fileUrlPrimitive = fileUrlAny.getAsJsonPrimitive();
            syncSchemaFileUrl = fileUrlPrimitive.isString() ? fileUrlPrimitive.getAsString() : syncSchemaFileUrl;
        } else
            Log.debug(SyncConfigValues.class, SCHEMA_FILE_URL_KEY + " not found or invalid.");

        this.syncOnStartup = autoSync;
        this.syncSchemaFileUrl = syncSchemaFileUrl;
    }

    public void readCompat(JsonObject root) {

        var autoSync = this.syncOnStartup;
        var autoSyncAny = root.get(COMPAT_AUTO_SYNC_KEY);
        if (autoSyncAny != null && autoSyncAny.isJsonPrimitive()) {
            var autoSyncPrimitive = autoSyncAny.getAsJsonPrimitive();
            autoSync = autoSyncPrimitive.isBoolean() && autoSyncPrimitive.getAsBoolean();
        }

        var syncSchemaFileUrl = this.syncSchemaFileUrl;
        var fileUrlAny = root.get(COMPAT_DOWNLOAD_URL_KEY);
        if (fileUrlAny != null && fileUrlAny.isJsonPrimitive()) {
            var fileUrlPrimitive = fileUrlAny.getAsJsonPrimitive();
            syncSchemaFileUrl = fileUrlPrimitive.isString() ? fileUrlPrimitive.getAsString() : syncSchemaFileUrl;
        }

        this.syncOnStartup = autoSync;
        this.syncSchemaFileUrl = syncSchemaFileUrl;
    }

    public boolean getSyncOnStartup() {
        return this.syncOnStartup;
    }
    public String getSyncSchemaFileUrl() {
        return this.syncSchemaFileUrl;
    }

    public void setSyncOnStartup(boolean syncOnStartup) {
        this.syncOnStartup = syncOnStartup;
    }
    public void setSyncSchemaFileUrl(String syncSchemaFileUrl) {
        this.syncSchemaFileUrl = syncSchemaFileUrl;
    }
}
