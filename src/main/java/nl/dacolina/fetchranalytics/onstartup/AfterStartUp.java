package nl.dacolina.fetchranalytics.onstartup;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class AfterStartUp {

    public static void afterStartUp(MinecraftServer server) {
        String[][] teams = retrieveTeamsFromServer(server);

        ArrayList<String> existingTeams = retrieveExistingTeamsInDatabase();

        System.out.println(existingTeams);


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
