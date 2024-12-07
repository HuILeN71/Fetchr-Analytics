package nl.dacolina.fetchranalytics.onstartup;

import nl.dacolina.fetchranalytics.database.FetchrCreateDatabaseObject;
import org.slf4j.Logger;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

public class OnStartUp {

    public static String[] errors;

    public static String[] runStartUpProgram() {
        FetchrAnalytics.LOGGER.info("Running start-up checks!");

        // check for datapack available

        // Create a config object
        Config config = new Config();

        // Check if config has errors
        if (config.error.isEmpty()) {
            errors[errors.length - 1] = config.error;
        }

        // Make sure database is reachable
        if(CheckDatabase.ableToConnect(config.getConnectString(), config.getDatabaseUser(), config.getUserPassword())) {

        }

        // Check whether database/ tables exist

        // if not exist create all tables

        // Load all items in table with their category

        // Set all the teams

        // Give ready status

        return errors;
    }

    private static boolean checkIfTablesExistInDatabase() {

        return false;
    }

}
