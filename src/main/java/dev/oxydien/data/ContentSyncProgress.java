package dev.oxydien.data;

import dev.oxydien.enums.ContentSyncOutcome;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ContentSyncProgress {

    private final int index;
    private final AtomicInteger progress;
    private AtomicReference<ContentSyncOutcome> outcome;
    private AtomicReference<Exception> exception;

    public ContentSyncProgress(int index, int progress) {
        this.index = index;
        this.progress = new AtomicInteger(progress);
        this.outcome = new AtomicReference<>(ContentSyncOutcome.IN_PROGRESS);
        this.exception = new AtomicReference<>(null);
    }

    public int getIndex() {
        return this.index;
    }

    public int getProgress() {
        return this.progress.get();
    }

    public ContentSyncOutcome getOutcome() {
        return this.outcome.get();
    }

    public Exception getException() {
        return this.exception.get();
    }

    public void setProgress(int progress) {
        this.progress.set(progress);
    }

    public void setOutcome(ContentSyncOutcome outcome, @Nullable Exception exception) {
        this.outcome.set(outcome);
        this.exception.set(exception);
    }

    public boolean isError() {
        var outcome = this.getOutcome();
        return outcome == ContentSyncOutcome.INVALID_URL || outcome == ContentSyncOutcome.DOWNLOAD_INTERRUPTED ||
                outcome == ContentSyncOutcome.ALREADY_EXISTS;
    }
}
