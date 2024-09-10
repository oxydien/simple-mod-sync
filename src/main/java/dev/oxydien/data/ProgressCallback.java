package dev.oxydien.data;

import dev.oxydien.enums.CallbackReason;

public interface ProgressCallback {
    void simple_mod_sync$onProgressUpdate(CallbackReason reason);
}
