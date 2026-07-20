package dev.oxydien.simpleModSync.config;

import com.google.gson.JsonObject;
import dev.oxydien.simpleModSync.log.Log;
import org.jetbrains.annotations.NotNull;

public class SeenConfigValues implements IConfigCodec {
    private static final String INIT_SCREEN_KEY = "init_screen";
    private static final String SYNC_PROGRESS_POPUP_KEY = "allow_sync_progress_popup";
    private static final String NEEDS_RESTART_POPUP_KEY = "allow_needs_restart_popup";
    private static final String CURSEFORGE_CONSTRAINTS = "curseforge_constraints";

    private boolean initScreen = false;
    private boolean allowSyncInProgressPopups = true;
    private boolean allowGameNeedsRestartPopups = true;
    private boolean curseforgeConstraints = false;

    public static @NotNull SeenConfigValues createDefault() {
        return new SeenConfigValues();
    }

    @Override
    public void writeTo(JsonObject parent) {
        parent.addProperty(INIT_SCREEN_KEY, initScreen);
        parent.addProperty(SYNC_PROGRESS_POPUP_KEY, allowSyncInProgressPopups);
        parent.addProperty(NEEDS_RESTART_POPUP_KEY, allowGameNeedsRestartPopups);
        parent.addProperty(CURSEFORGE_CONSTRAINTS, curseforgeConstraints);
    }

    @Override
    public void readFrom(JsonObject parent) {

        var initScreen = this.initScreen;
        var initScreenAny = parent.get(INIT_SCREEN_KEY);
        if (initScreenAny != null && initScreenAny.isJsonPrimitive()) {
            var initScreenPrimitive = initScreenAny.getAsJsonPrimitive();
            initScreen = initScreenPrimitive.isBoolean() && initScreenPrimitive.getAsBoolean();
        } else
            Log.debug(SeenConfigValues.class, INIT_SCREEN_KEY + " not found or invalid.");

        var allowSyncInProgressPopups = this.allowSyncInProgressPopups;
        var allowSyncInProgressPopupsAny = parent.get(SYNC_PROGRESS_POPUP_KEY);
        if (allowSyncInProgressPopupsAny != null && allowSyncInProgressPopupsAny.isJsonPrimitive()) {
            var allowSyncInProgressPopupsPrimitive = allowSyncInProgressPopupsAny.getAsJsonPrimitive();
            allowSyncInProgressPopups = allowSyncInProgressPopupsPrimitive.isBoolean() && allowSyncInProgressPopupsPrimitive.getAsBoolean();
        } else
            Log.debug(SeenConfigValues.class, SYNC_PROGRESS_POPUP_KEY + " not found or invalid.");

        var allowGameNeedsRestartPopups = this.allowGameNeedsRestartPopups;
        var allowGameNeedsRestartPopupsAny = parent.get(NEEDS_RESTART_POPUP_KEY);
        if (allowGameNeedsRestartPopupsAny != null && allowGameNeedsRestartPopupsAny.isJsonPrimitive()) {
            var allowGameNeedsRestartPopupsPrimitive = allowGameNeedsRestartPopupsAny.getAsJsonPrimitive();
            allowGameNeedsRestartPopups = allowGameNeedsRestartPopupsPrimitive.isBoolean() && allowGameNeedsRestartPopupsPrimitive.getAsBoolean();
        } else
            Log.debug(SeenConfigValues.class, NEEDS_RESTART_POPUP_KEY + " not found or invalid.");

        var curseforgeConstraints = this.curseforgeConstraints;
        var curseforgeConstraintsAny = parent.get(CURSEFORGE_CONSTRAINTS);
        if (curseforgeConstraintsAny != null && curseforgeConstraintsAny.isJsonPrimitive()) {
            var curseforgeConstraintsPrimitive = curseforgeConstraintsAny.getAsJsonPrimitive();
            curseforgeConstraints = curseforgeConstraintsPrimitive.isBoolean() && curseforgeConstraintsPrimitive.getAsBoolean();
        } else
            Log.debug(SeenConfigValues.class, CURSEFORGE_CONSTRAINTS + " not found or invalid.");

        this.initScreen = initScreen;
        this.allowSyncInProgressPopups = allowSyncInProgressPopups;
        this.allowGameNeedsRestartPopups = allowGameNeedsRestartPopups;
        this.curseforgeConstraints = curseforgeConstraints;
    }

    public boolean hasVisitedInitScreen() {
        return this.initScreen;
    }
    public boolean isSyncInProgressPopupsAllowed() {
        return this.allowSyncInProgressPopups;
    }
    public boolean isSyncGameNeedsRestartAllowed() {
        return this.allowGameNeedsRestartPopups;
    }
    public boolean hasVisitedCurseforgeConstraints() { return this.curseforgeConstraints; }

    public void setHasVisitedInitScreen(boolean hasVisited) {
        this.initScreen = hasVisited;
    }
    public void setAllowSyncInProgressPopups(boolean allowed) {
        this.allowSyncInProgressPopups = allowed;
    }
    public void setAllowGameNeedsRestartPopups(boolean allowed) {
        this.allowGameNeedsRestartPopups = allowed;
    }
    public void setHasVisitedCurseforgeConstraints(boolean hasVisited) {
        this.curseforgeConstraints = hasVisited;
    }
}
