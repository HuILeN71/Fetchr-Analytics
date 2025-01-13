package nl.dacolina.fetchranalytics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import nl.dacolina.fetchranalytics.onstartup.AfterStartUp;
import nl.dacolina.fetchranalytics.onstartup.OnStartUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchrAnalytics implements ModInitializer {
	public static final String MOD_ID = "fetchranalytics";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		FetchrAnalytics.LOGGER.info("Starting fetchranalytics v0.0.1...");

		String[] errors = OnStartUp.runStartUpProgram();

		FetchrAnalytics.LOGGER.info("Finished loading mod!");

		// Register a callback for when the server starts
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
	}

	private void onServerStarted(MinecraftServer server) {
		FetchrAnalytics.LOGGER.info("Running after start-up checks!");
		AfterStartUp.afterStartUp(server);
	}
}