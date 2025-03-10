package nl.dacolina.fetchranalytics.onstartup;

import nl.dacolina.fetchranalytics.managers.DatabaseManager;
import org.slf4j.Logger;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

public class OnStartUp {

    public static String[] errors = {"N"};

    public static String[] runStartUpProgram() {
        FetchrAnalytics.LOGGER.debug("Running start-up checks!");

        // Create a config object
        Config config = new Config();

        // Check if config has errors
        if (config.error.isEmpty()) {
            errors[0] = config.error;
        }

        DatabaseManager.createConnectionPool(config.getConnectString(), config.getDatabaseUser(), config.getUserPassword());

        // Make sure database is reachable
        if(CheckDatabase.ableToConnect()) {
            // Check whether database/ tables exist

            FetchrAnalytics.LOGGER.info("Database is reachable and ready to be used!");

            if (!CheckDatabase.areAllTablesAvailable(config.getDatabaseName())) {
                // if not exist create all tables
                FetchrAnalytics.LOGGER.info("Not all tables exist or database has not yet been initialized!");

                CheckDatabase.constructDatabase();

            }

        } else {
            errors[1] = "Database cannot be reached at this time. Please make sure your settings are correct.";
        }

        return errors;
    }

    private static boolean checkIfTablesExistInDatabase() {

        return false;
    }

}
