package nl.dacolina.fetchranalytics.onstartup;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class AfterStartUp {

    public static void afterStartUp(MinecraftServer server) {

        // Checking if all teams are present in the database!
        String[][] teams = retrieveTeamsFromServer(server);

        ArrayList<String> existingTeams = retrieveExistingTeamsInDatabase();

        String[][] teamsToAddDatabase = checkingForMissingTeams(teams, existingTeams);

        if (teamsToAddDatabase[0][0] != null) {

            FetchrAnalytics.LOGGER.info("Missing some teams, adding them!");
            addTeamsToDatabase(teamsToAddDatabase);
        }

        // Check if all categories are loaded in the database!

        // Check which categories are already in the database
        ArrayList<String> missingCategories = Categories.getMissingCategories(server);

        // Add missing categories to database, if arrayList has contents

        if(!missingCategories.isEmpty()) {
            Categories.addMissingCategoriesToDatabase(missingCategories);
        }

        // Init items

        Items.getItemsFromGame(server);


    }

    public static String[][] checkingForMissingTeams(String[][] teamsFromGame, ArrayList<String> teamsFromDatabase) {

        String[][] missingTeams = new String[teamsFromGame.length][2];

        int counter = 0;

        for (int i = 0; i < teamsFromGame.length; i++) {
            if (!teamsFromDatabase.contains(teamsFromGame[i][0])) {

                missingTeams[counter][0] = teamsFromGame[i][0];
                missingTeams[counter][1] = teamsFromGame[i][1];

                counter++;

//                 Debug
                 FetchrAnalytics.LOGGER.debug(teamsFromGame[i][0] + " ---- " + teamsFromGame[i][1]);


            }



        }

        return missingTeams;

    }

    public static String[][] retrieveTeamsFromServer (MinecraftServer server) {

        Scoreboard scoreboard = server.getScoreboard();

        Collection<Team> teams = scoreboard.getTeams();

        String[][] teamsArray = new String[teams.size()][2];

        //Test for all teams

        int counter = 0;

        for (Team team : teams) {
            teamsArray[counter][0] = team.getName();
            counter++;
        }

        teamsArray = craftDisplayName(teamsArray);

//        for (int i = 0; i < teamsArray.length; i++) {
//            FetchrAnalytics.LOGGER.info(teamsArray[i][0] + " | " + teamsArray[i][1]);
//
//        }

        return teamsArray;

    }

    public static boolean addTeamsToDatabase(String[][] teamsToAdd) {

        try {
            Connection dbConn = DatabaseManager.getConnection();

            // Create query

            StringBuilder query = new StringBuilder("INSERT INTO teams (fetchr_team_id, displayName) VALUES ");

            for (int i = 0; i < teamsToAdd.length; i++) {

                if(i != teamsToAdd.length - 1) {
                    query.append("(?, ?),");
                } else {
                    query.append("(?, ?);");
                }

                FetchrAnalytics.LOGGER.debug(String.valueOf(query));

            }

            // Prepare query

            PreparedStatement stmt = dbConn.prepareStatement(String.valueOf(query));

            // Fill all the question marks

            int j = 1;

            for (int i = 0; i < teamsToAdd.length; i++) {
                stmt.setString(j, teamsToAdd[i][0]);

                j++;

                stmt.setString(j, teamsToAdd[i][1]);

                j++;

            }

            stmt.execute();


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String[][] craftDisplayName(String[][] teams) {

        int counter = 0;

        for (String[] team : teams) {
            String[] parts = team[0].split("\\.");

            String temp = parts[1].replace("_", " ");

            temp = temp.substring(0,1).toUpperCase() + temp.substring(1);

//            FetchrAnalytics.LOGGER.info(team[0]);

            teams[counter][1] = temp;

            counter++;

        }

        return teams;
    }

    private static ArrayList<String> retrieveExistingTeamsInDatabase() {

        ArrayList<String> existingTables = new ArrayList<String>();

        try {
            Connection dbConn = DatabaseManager.getConnection();

            String query = "SELECT fetchr_team_id FROM teams";

            PreparedStatement stmt = dbConn.prepareStatement(query);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                existingTables.add(rs.getString(1));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return existingTables;

    }


}
