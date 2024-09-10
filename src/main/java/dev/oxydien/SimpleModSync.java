package dev.oxydien;

import dev.oxydien.config.Config;
import dev.oxydien.workers.ModDownloadWorker;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleModSync implements ModInitializer {
	public static final String MOD_ID = "simple-mod-sync";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ModDownloadWorker worker;

	@Override
	public void onInitialize() {
		LOGGER.info("Simple Mod Sync is starting up...");
		FabricLoader loader = FabricLoader.getInstance();
		String configPath = loader.getConfigDir() + "/" + MOD_ID + ".json";
		String modsPath = loader.getGameDir() + "/mods";
		new Config(configPath, modsPath);
		SimpleModSync.StartWorker();
	}

	public static void StartWorker() {
		worker = new ModDownloadWorker();
		Thread thread = new Thread(worker);
		thread.start();
	}
}
