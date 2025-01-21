package nl.dacolina.fetchranalytics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import nl.dacolina.fetchranalytics.managers.GameManager;
import nl.dacolina.fetchranalytics.onstartup.AfterStartUp;
import nl.dacolina.fetchranalytics.onstartup.OnStartUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchrAnalytics implements ModInitializer {
	public static final String MOD_ID = "fetchranalytics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private GameManager bingoManager;

	@Override
	public void onInitialize() {
		FetchrAnalytics.LOGGER.info("Starting fetchranalytics v0.0.1...");

		String[] errors = OnStartUp.runStartUpProgram();

		FetchrAnalytics.LOGGER.info("Finished loading mod!");

		// Register a callback for when the server starts
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
	}

	private void onServerStarted(MinecraftServer server) {
		// Start game checks, see if database is ready to receive data.
		FetchrAnalytics.LOGGER.info("Running after start-up checks...");
		AfterStartUp.afterStartUp(server);
		FetchrAnalytics.LOGGER.info("Finished start-up checks!");

		// Start a GameManager Class to manage all the games
		bingoManager = new GameManager(server);

		// Register event where something is to be checked every tick
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

	}

	private void onServerTick(MinecraftServer server) {
		bingoManager.tick();
	}
}