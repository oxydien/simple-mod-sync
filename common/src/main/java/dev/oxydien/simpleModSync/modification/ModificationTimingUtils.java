package dev.oxydien.simpleModSync.modification;

import dev.oxydien.simpleModSync.log.Log;

import java.util.Objects;

public class ModificationTimingUtils {
    public static ModificationTiming FromString(String timing) {
        if (Objects.equals(timing, "pre-sync")) return ModificationTiming.PreSync;
        if (Objects.equals(timing, "after-sync")) return ModificationTiming.AfterSync;
        if (!timing.isEmpty()) {
            Log.warning("Unknown modification timing: " + timing);
            return ModificationTiming.AfterSync;
        }
        return ModificationTiming.AfterSync;
    }
}
