package dev.oxydien.simpleModSync;

import dev.oxydien.simpleModSync.config.Config;
import dev.oxydien.simpleModSync.content.SyncSchema;
import dev.oxydien.simpleModSync.log.Log;
import dev.oxydien.simpleModSync.ui.modals.GameNeedsRestartModalHandler;
import dev.oxydien.simpleModSync.ui.modals.SyncInProgressModalHandler;
import dev.oxydien.simpleModSync.workers.SyncWorker;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public abstract class SimpleModSync {
    public static final String MOD_ID = "simplemodsync";

    private static SimpleModSync INSTANCE;
    
    public static void init(SimpleModSync instance) {
        INSTANCE = instance;
    }
    
    public static SimpleModSync getInstance() {
        return INSTANCE;
    }
    
    public abstract Path getInstanceDir();

    public HandlerRegistry Handlers = new HandlerRegistry();
    private final AtomicReference<Thread> workerThread = new AtomicReference<>();

    @Nullable
    public SyncSchema syncSchema;
    public SyncWorker syncWorker;

    private SyncInProgressModalHandler _syncInProgressModal;
    private GameNeedsRestartModalHandler _gameNeedsRestartModal;

    public void onInitialize() {
        Log.init(MOD_ID);
        Log.debug("Initializing SimpleModSync");

        Path configPath = this.getInstanceDir().resolve("config").resolve(MOD_ID + ".json");
        new Config(configPath);

        this.Handlers.init();

        if (Config.instance.getSyncOnStartup()) {
            this.start();
        }
    }

    public void start() {
        if  (this.syncWorker != null && this.syncWorker.isRunning()) {
            return;
        }

        Log.info("Starting SimpleModSync background worker thread...");

        this.syncSchema = new SyncSchema();
        this.syncWorker = new SyncWorker(this.syncSchema);

        this._syncInProgressModal = new SyncInProgressModalHandler();
        this._gameNeedsRestartModal = new GameNeedsRestartModalHandler();

        Thread thread = new Thread(this.syncWorker);
        this.workerThread.set(thread);
        thread.start();
    }

    public void stop() { // warn: unused
        if (this.syncWorker != null) {
            this.syncWorker.shutdown();
            this.syncWorker = null;
        }

        Thread thread = workerThread.get();
        if (thread != null) {
            thread.interrupt();
        }
    }
}
