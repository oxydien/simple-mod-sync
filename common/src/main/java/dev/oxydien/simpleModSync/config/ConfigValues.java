package dev.oxydien.simpleModSync.config;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigValues implements IConfigCodec {
    private static final String SEEN_CONFIG_KEY = "seen";
    private static final String SYNC_CONFIG_KEY = "sync";

    private SeenConfigValues seenConfig = SeenConfigValues.createDefault();
    private SyncConfigValues syncConfig = SyncConfigValues.createDefault();

    public static @NotNull ConfigValues createDefault() {
        return new ConfigValues();
    }

    @Override
    public void writeTo(JsonObject parent) {
        var seenObj = new JsonObject();
        this.seenConfig.writeTo(seenObj);

        var syncObj = new JsonObject();
        this.syncConfig.writeTo(syncObj);

        parent.add(SEEN_CONFIG_KEY, seenObj);
        parent.add(SYNC_CONFIG_KEY, syncObj);
    }

    @Override
    public void readFrom(JsonObject parent) {
        SeenConfigValues seenConfig = SeenConfigValues.createDefault();
        var seenAny = parent.get(SEEN_CONFIG_KEY);
        if (seenAny != null && seenAny.isJsonObject()) {
            var seenObj = seenAny.getAsJsonObject();
            seenConfig.readFrom(seenObj);
        }

        SyncConfigValues syncConfig = SyncConfigValues.createDefault();
        var syncAny = parent.get(SYNC_CONFIG_KEY);
        if (syncAny != null && syncAny.isJsonObject()) {
            var syncObj = syncAny.getAsJsonObject();
            syncConfig.readFrom(syncObj);
        }

        // Compatibility with old version of the config
        syncConfig.readCompat(parent);
        if (!syncConfig.getSyncSchemaFileUrl().isEmpty()) {
            seenConfig.setHasVisitedInitScreen(true);
            if (Objects.equals(syncConfig.getSyncSchemaFileUrl(), "-")) {
                syncConfig.setSyncSchemaFileUrl("");
            }
        }

        this.seenConfig = seenConfig;
        this.syncConfig = syncConfig;
    }

    public SeenConfigValues getSeenConfig() {
        return this.seenConfig;
    }
    public SyncConfigValues getSyncConfig() {
        return this.syncConfig;
    }
}
