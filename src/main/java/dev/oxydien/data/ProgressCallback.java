package dev.oxydien.data;

import dev.oxydien.enums.CallbackReason;

public interface ProgressCallback {
    void onProgress(CallbackReason reason);
}
