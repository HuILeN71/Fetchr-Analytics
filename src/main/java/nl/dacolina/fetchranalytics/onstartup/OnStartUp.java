package nl.dacolina.fetchranalytics.onstartup;

import nl.dacolina.fetchranalytics.database.FetchrCreateDatabaseObject;
import org.slf4j.Logger;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

public class OnStartUp {

    public static boolean runStartUpProgram() {
        FetchrAnalytics.LOGGER.info("Running start-up checks!");

        // check for datapack available

        // Check for settings file: fetchr-analytics-settings.toml

        // If not exist create a file

        // read the file

        // Check wether database/ tables exist

        // if not exist create all tables

        // Load all items in table with their category

        // Set all the teams

        // Give ready status

        return false;
    }

    private static boolean checkIfTablesExistInDatabase() {

        return false;
    }

}
