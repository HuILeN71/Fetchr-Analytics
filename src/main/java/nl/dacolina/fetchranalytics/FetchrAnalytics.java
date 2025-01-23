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
	private static int startDelay = 80; // Four seconds
	private static boolean wasChecked = false;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private GameManager bingoManager;

	@Override
	public void onInitialize() {
		FetchrAnalytics.LOGGER.info("Starting fetchranalytics v0.0.1...");

		String[] errors = OnStartUp.runStartUpProgram();

		FetchrAnalytics.LOGGER.info("Finished loading mod!");

		// Register a callback for when the server starts
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

	}


	private void onServerTick(MinecraftServer server) {
		// Wait two seconds to execute next logic

		if (!wasChecked) {
			if(startDelay > 0) {
				// Count down until delay has passed
				startDelay--;
			} else {
				FetchrAnalytics.LOGGER.info("Running after start-up checks...");
				AfterStartUp.afterStartUp(server);
				FetchrAnalytics.LOGGER.info("Finished start-up checks!");

				bingoManager = new GameManager(server);

				wasChecked = true;

			}

		} else {
			bingoManager.tick();
		}


	}
}