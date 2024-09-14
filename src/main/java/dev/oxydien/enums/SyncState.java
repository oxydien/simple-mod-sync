package dev.oxydien.enums;

public enum SyncState {
    INITIALIZING,
    CHECKING_REMOTE,
    PARSING_REMOTE,
    DOWNLOADING,
    READY,
    DID_NOT_SYNC,
    NEEDS_RESTART,
    ERROR
}
