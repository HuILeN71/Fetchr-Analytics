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
		String[] errors = OnStartUp.runStartUpProgram();

		// Register a callback for when the server starts
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
	}

	private void onServerStarted(MinecraftServer server) {
		FetchrAnalytics.LOGGER.info("Test if this runs after server has started!!");
		AfterStartUp.afterStartUp(server);
	}
}