package nl.dacolina.fetchranalytics.components;

import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;
import nl.dacolina.fetchranalytics.managers.ItemManager;
import org.mariadb.jdbc.Statement;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Game {

    private int gameID;
    private int playerCount;
    private boolean isInitialized;
    private List<Team> teams;
    private int seed;

    public Game(MinecraftServer server) {
        // Set init value to false
        this.isInitialized = false;
        this.playerCount = 0;
        this.gameID = 0;

        // Init bingo board
        BingoCard bingoBoard = new BingoCard(server);

        // Init teams
        this.teams = getTeamsFromCurrentGame(server);
        outputTeamsToConsole(teams);

        this.seed = getSeedFromGame(server);

        // Check if all players are in the database
        checkForPlayersInDatabase(teams);

        // Add current game to database
        addNewGameToDatabase(teams, seed, 1);

        this.isInitialized = true;
    }

    public void tick(MinecraftServer server) {
        // Get current time from neunEinser time scoreboard in game
        String timeItemGotten = getTimeFromInGameScoreboard(server);

        FetchrAnalytics.LOGGER.info(timeItemGotten);

        // Check players for updated item count
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
                this.playerCount++;
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
        ScoreboardObjective objective = scoreboard.getNullableObjective("fetchr.state");

        if(objective != null) {
            ScoreHolder gameStatePlayer = ScoreHolder.fromName("$seed");
            ReadableScoreboardScore gameStateScore = scoreboard.getScore(gameStatePlayer, objective);
            return gameStateScore.getScore();
        }


        return -1;
    }

    private void addNewGameToDatabase(List<Team> teamsInGame, int seed, int serverID) {
        try {
            // Create a new game query
            String queryOne = "INSERT INTO game (seed, server_id) VALUES (?, ?)";

            String queryTwo = "INSERT INTO teamInGame (game_id, teams_id, player_id) VALUES " + ItemManager.argumentsBuilderDatabaseQuery(3, this.playerCount);

            Connection dbConn = DatabaseManager.getConnection();

            // Set query in "transaction mode"
            dbConn.setAutoCommit(false);

            PreparedStatement stmtOne = dbConn.prepareStatement(queryOne, Statement.RETURN_GENERATED_KEYS);

            stmtOne.setInt(1, seed);
            stmtOne.setInt(2, serverID);

            stmtOne.executeUpdate();

            // Get created game ID
            ResultSet generatedKeys = stmtOne.getGeneratedKeys();
            int gameID = 0;

            if(generatedKeys.next()) {
                gameID = generatedKeys.getInt(1);
            }

            this.gameID = gameID;

            PreparedStatement stmtTwo = dbConn.prepareStatement(queryTwo);

            int counter = 1;

            for (Team team : teamsInGame) {
                for(Player teamMember : team.getTeamMembers()) {
                    // Set game id
                    stmtTwo.setInt(counter, gameID);
                    counter++;

                    // Set team name
                    stmtTwo.setString(counter, team.getTeamName());
                    counter++;

                    // Set team member
                    stmtTwo.setString(counter, String.valueOf( teamMember.getPlayerUUID()));
                    counter++;

                }
            }

            stmtTwo.executeUpdate();
            dbConn.commit();

            dbConn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getTimeFromInGameScoreboard(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective("91.timer.io");

        if(objective != null) {
            ScoreHolder hoursPlayer = ScoreHolder.fromName("$hours");
            ScoreHolder minutesPlayer = ScoreHolder.fromName("$minutes");
            ScoreHolder secondsPlayer = ScoreHolder.fromName("$seconds");
            ScoreHolder hundredsPlayer = ScoreHolder.fromName("$millis");

            ReadableScoreboardScore hoursScore = scoreboard.getScore(hoursPlayer, objective);
            ReadableScoreboardScore minutesScore = scoreboard.getScore(minutesPlayer, objective);
            ReadableScoreboardScore secondsScore = scoreboard.getScore(secondsPlayer, objective);
            ReadableScoreboardScore hundredsScore = scoreboard.getScore(hundredsPlayer, objective);

            return hoursScore.getScore() + ":" + minutesScore.getScore() + ":" + secondsScore.getScore() + "." + hundredsScore.getScore();
        }

        return "0:0:0.0";
    }


}
