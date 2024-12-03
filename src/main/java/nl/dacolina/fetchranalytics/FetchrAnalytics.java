package nl.dacolina.fetchranalytics;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchrAnalytics implements ModInitializer {
	public static final String MOD_ID = "fetchranalytics";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String DB_URL = "jdbc:mysql://10.0.10.10:64123/fetchr-analytics";
	private static final String USER = "fetchr";
	private static final String PASSWORD = "testPassword!";

	@Override
	public void onInitialize() {

	}
}