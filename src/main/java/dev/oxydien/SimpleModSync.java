package dev.oxydien;

import dev.oxydien.config.Config;
import dev.oxydien.logger.Log;
import dev.oxydien.workers.ModDownloadWorker;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;

public class SimpleModSync implements ModInitializer {
	public static final String MOD_ID = "simple-mod-sync";

	public static ModDownloadWorker worker;

	@Override
	public void onInitialize() {
		new Log(MOD_ID, true);
		Log.Log.info("Simple Mod Sync is starting up...");
		FabricLoader loader = FabricLoader.getInstance();
		String configPath = loader.getConfigDir() + "/" + MOD_ID + ".json";
		String destPath = loader.getGameDir().toString();
		new Config(configPath, destPath);

		worker = new ModDownloadWorker();
		if (Config.instance.getAutoDownload()) {
			SimpleModSync.StartWorker();
		}
	}

	public static void StartWorker() {
		if (worker.GetProgress() != 0 && worker.GetProgress() != 100) {
			Log.Log.debug("start-worker", "Worker already started {}", worker.GetProgress());
			return;
		}
		worker.start();
	}
}
