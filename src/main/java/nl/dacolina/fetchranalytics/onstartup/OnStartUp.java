package nl.dacolina.fetchranalytics.onstartup;

import nl.dacolina.fetchranalytics.database.FetchrCreateDatabaseObject;
import org.slf4j.Logger;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

public class OnStartUp {

    public static String[] errors = {"N"};

    public static String[] runStartUpProgram() {
        FetchrAnalytics.LOGGER.info("Running start-up checks!");

        // check for datapack available

        // Create a config object
        Config config = new Config();

        // Check if config has errors
        if (config.error.isEmpty()) {
            errors[0] = config.error;
        }

        // Make sure database is reachable
        if(CheckDatabase.ableToConnect(config.getConnectString(), config.getDatabaseUser(), config.getUserPassword())) {
            // Check whether database/ tables exist

            FetchrAnalytics.LOGGER.info("Database is reachable and ready to be used!");

            if (!CheckDatabase.areAllTablesAvailable(config.getConnectString(), config.getDatabaseUser(), config.getUserPassword(), config.getDatabaseName())) {
                // if not exist create all tables
                FetchrAnalytics.LOGGER.info("Not all tables exist or database has not yet been initialized!");


            }

        } else {
            errors[1] = "Database cannot be reached at this time. Please make sure your settings are correct.";
        }

        // Load all items in table with their category

        // Set all the teams

        // Give ready status

        return errors;
    }

    private static boolean checkIfTablesExistInDatabase() {

        return false;
    }

}
