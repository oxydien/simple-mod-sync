package dev.oxydien.data;

import dev.oxydien.enums.ContentSyncOutcome;
import org.jetbrains.annotations.Nullable;

public class ContentSyncProgress {

    private final int index;
    private int progress;
    private ContentSyncOutcome outcome;
    private Exception exception;

    public ContentSyncProgress(int index, int progress) {
        this.index = index;
        this.progress = progress;
        this.outcome = ContentSyncOutcome.IN_PROGRESS;
        this.exception = null;
    }

    public int getIndex() {
        return this.index;
    }

    public int getProgress() {
        return this.progress;
    }

    public ContentSyncOutcome getOutcome() {
        return this.outcome;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setOutcome(ContentSyncOutcome outcome, @Nullable Exception exception) {
        this.outcome = outcome;
        this.exception = exception;
    }

    public boolean isError() {
        return this.outcome == ContentSyncOutcome.INVALID_URL || this.outcome == ContentSyncOutcome.DOWNLOAD_INTERRUPTED ||
                this.outcome == ContentSyncOutcome.ALREADY_EXISTS;
    }
}
