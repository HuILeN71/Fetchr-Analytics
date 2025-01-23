package nl.dacolina.fetchranalytics.components;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Game {

    private boolean isInitialized;
    private List<Team> teams;

    public Game(MinecraftServer server) {
        // Set init value to false
        this.isInitialized = false;

        // Init bingo board
        BingoCard bingoBoard = new BingoCard(server);

        // Init teams
        this.teams = getTeamsFromCurrentGame(server);
        outputTeamsToConsole(teams);

        // Check if all players are in the database
        checkForPlayersInDatabase(teams);

        //Add current game to database
        addNewGameToDatabase(teams);

        this.isInitialized = true;
    }

    public boolean getIsInitialized() {
        return this.isInitialized;
    }

    private List<Team> getTeamsFromCurrentGame(MinecraftServer server) {
        // Init team list
        List<Team> populatedTeams = new ArrayList<>();
        int memberCount = 0;

        Scoreboard scoreboard = server.getScoreboard();
        Collection<net.minecraft.scoreboard.Team> teamsFromGame = scoreboard.getTeams();

        for (net.minecraft.scoreboard.Team team : teamsFromGame) {
            Collection<String> members = team.getPlayerList();

            Team newTeam = new Team(team.getName());

            for (String memberName : members) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(memberName);
                if (player != null) {
                    // Get player UUID since that is unique in database
                    memberCount++;
                    UUID uuid = player.getUuid();
                    newTeam.addTeamMember(uuid, memberName);
                }
            }

            if (memberCount > 0) {
                populatedTeams.add(newTeam);
            }

            memberCount = 0;
        }

        return populatedTeams;
    }

    private void checkForPlayersInDatabase(List<Team> teamsInGame) {

        for (Team team : teamsInGame) {
            for(Player teamMember : team.getTeamMembers()) {
                teamMember.checkForPlayerDatabaseEntry(teamMember.getPlayerUUID(), teamMember.getPlayerName());
            }
        }

    }

    private void outputTeamsToConsole(List<Team> teamsInGame) {

        for (Team team : teamsInGame) {
            for(Player teamMember : team.getTeamMembers()) {
                FetchrAnalytics.LOGGER.info(team.getTeamName() + ": " + teamMember.getPlayerName() + " (" + teamMember.getPlayerUUID() + ")");
            }
        }

    }

    private int getSeedFromGame(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        return -1;
    }

    private void addNewGameToDatabase(List<Team> teamsInGame) {
        try {
            String query = "";
            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
