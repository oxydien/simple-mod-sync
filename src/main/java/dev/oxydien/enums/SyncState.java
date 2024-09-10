package dev.oxydien.enums;

public enum SyncState {
    INITIALIZING,
    CHECKING_REMOTE,
    PARSING_REMOTE,
    DOWNLOADING,
    READY,
    NEEDS_RESTART,
    ERROR
}
