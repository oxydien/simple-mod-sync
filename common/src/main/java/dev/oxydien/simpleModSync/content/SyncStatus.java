package dev.oxydien.simpleModSync.content;

import dev.oxydien.simpleModSync.log.Log;
import net.minecraft.network.chat.Component;

public class SyncStatus {
    public enum SyncState {
        STARTING,
        PARSING,
        FINISHED,
        DOWNLOADING,
        MODIFIED,
        UNSYNCED,
        RETRIEVING_SCHEMA,
        INVALID,
        UNSUPPORTED,
        ERROR,
    }

    public static Component TranslatedState(SyncState state) {
        return switch (state) {
            case STARTING -> Component.translatable("simple_mod_sync.ui.sync_state.starting");
            case PARSING ->  Component.translatable("simple_mod_sync.ui.sync_state.parsing");
            case FINISHED ->   Component.translatable("simple_mod_sync.ui.sync_state.finished");
            case DOWNLOADING -> Component.translatable("simple_mod_sync.ui.sync_state.downloading");
            case MODIFIED ->  Component.translatable("simple_mod_sync.ui.sync_state.modified");
            case UNSYNCED -> Component.translatable("simple_mod_sync.ui.sync_state.unsynced");
            case RETRIEVING_SCHEMA -> Component.translatable("simple_mod_sync.ui.sync_state.retrieving_schema");
            case INVALID ->   Component.translatable("simple_mod_sync.ui.sync_state.invalid");
            case UNSUPPORTED ->  Component.translatable("simple_mod_sync.ui.sync_state.unsupported");
            case ERROR ->  Component.translatable("simple_mod_sync.ui.sync_state.error");
        };
    }

    private SyncState syncState;
    private float downloadProgress; // 0 -> 1
    private String errorMessage;

    public SyncStatus() {
        this.syncState = SyncState.STARTING;
        this.downloadProgress = 0;
        this.errorMessage = "";
    }

    public static SyncStatus ofState(SyncState state) {
        SyncStatus status = new SyncStatus();
        status.setState(state);
        return status;
    }

    public static SyncStatus ofError(String errorMessage) {
        SyncStatus status = new SyncStatus();
        status.setErrorMessage(errorMessage);
        return status;
    }

    public SyncState getState() {
        if (!this.errorMessage.isBlank()) {
            return SyncState.ERROR;
        }
        return this.syncState;
    }

    public void setState(SyncState state) {
        this.syncState = state;
    }

    public float getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(float downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isError() {
        return this.getState() == SyncState.ERROR;
    }
    public boolean isInProgress() {
        return this.getState() == SyncState.STARTING
                || this.getState() == SyncState.PARSING
                || this.getState() == SyncState.DOWNLOADING;
    }
    public boolean isRestartNeeded() {
        return this.getState() == SyncState.MODIFIED;
    }
}
