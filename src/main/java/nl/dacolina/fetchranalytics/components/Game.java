package nl.dacolina.fetchranalytics.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;
import nl.dacolina.fetchranalytics.managers.ItemManager;
import org.checkerframework.checker.units.qual.C;
import org.mariadb.jdbc.Statement;

import java.sql.*;
import java.util.*;

public class Game {

    private static final String TAG_REGEX = "fetchranalytics.has_slot.";

    private int gameID;
    private int playerCount;
    private boolean isInitialized;
    private List<Team> teams;
    private int seed;
    private BingoCard bingoBoard;

    public Game(MinecraftServer server) {
        // Set init value to false
        this.isInitialized = false;
        this.playerCount = 0;
        this.gameID = 0;

        // Init bingo board
        this.bingoBoard = new BingoCard(server);

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
        String timeInGame = getTimeFromInGameScoreboard(server);

        //FetchrAnalytics.LOGGER.info(timeInGame);

        // Check players for updated item count
        checkPlayersForTagCheck(server, timeInGame);

    }

    private void checkPlayersForTagCheck(MinecraftServer server, String timeInGame) {
         for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
             for(Team team : teams) {
                 for(Player teamMember : team.getTeamMembers()) {
                     if(player.getUuid() == teamMember.getPlayerUUID()) {
                         checkForTags(player, teamMember, team.getTeamName(), timeInGame, server);
                     }
                 }
             }
         }
    }

    private int checkForTags(ServerPlayerEntity player, Player teamMember, String teamName, String timeInGame, MinecraftServer server) {
        Set<String> tags = player.getCommandTags();

        if(tags.isEmpty()) {
            return -1;
        }

        if(tags.size() > teamMember.getLastCountOfTags()) {
            teamMember.setLastCountOfTags(tags.size());
            for (String tag : tags) {
                if(tag.startsWith(TAG_REGEX)) {
                    int itemNumber = Integer.parseInt(tag.substring(TAG_REGEX.length()));

                    if(!teamMember.getCollectedItems().containsKey(itemNumber)) {
                        teamMember.addCollectedItem(itemNumber, timeInGame);
                        addGottenItemToDatabase(teamMember.getPlayerUUID(), itemNumber, timeInGame);
                        // FetchrAnalytics.LOGGER.info(teamMember.getPlayerName() + " collected item " + itemNumber + " at time " + timeInGame);

                        checkForBingoRow(server, teamName);

                    }
                    // FetchrAnalytics.LOGGER.info(tag);

                }


            }
        }

        return -1;

    }

    private boolean checkForBingoRow(MinecraftServer server, String teamName) {
//        Identifier storageId = Identifier.of("fetchr", "card");
//
//        NbtCompound nbt = server.getDataCommandStorage().get(storageId);
//
//        NbtList teams = nbt.getList("teams", NbtElement.COMPOUND_TYPE);
//
//        for (int i = 0; i < teams.size(); i++) {
//            NbtCompound team = teams.getCompound(i);
//
//            FetchrAnalytics.LOGGER.info(team.getString("id"));
//
//            if (teamName.equals(team.getString("id"))) {
//                // Check the has_bingo value
//
//                if(team.contains("has_bingo")) {
//                    FetchrAnalytics.LOGGER.info("Contains the has_bingo value");
//
//                    boolean hasBingo = team.getBoolean("has_bingo");
//
//                    if(hasBingo) {
//                        FetchrAnalytics.LOGGER.info("Team " + teamName + " has a bingo row or column.");
//                    } else {
//                        FetchrAnalytics.LOGGER.info("Does not have bingo!");
//                    }
//                } else {
//                    FetchrAnalytics.LOGGER.info("Value does not exist yet (has_bingo)");
//                }
//            }
//        }
//
        return false;
    }

    private void addGottenItemToDatabase(UUID playerUUID, int gottenItem, String timeGotten) {
        String query = "INSERT INTO itemsInGame (bingo_card_id, itemNumber, player_id, timeGotten) VALUES (?, ?, ?, ?)";

        try {
            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query);

            // Set bingocard
            stmt.setInt(1, this.bingoBoard.getBingoCardID());

            // set item
            stmt.setInt(2, gottenItem);

            // Set player id
            stmt.setString(3, String.valueOf(playerUUID));

            // timeGottem
            stmt.setString(4, timeGotten);

            stmt.execute();

            dbConn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

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

            String queryThree = "SELECT COALESCE(MAX(bingo_card_id), 0) + 1 AS next_id FROM bingoCardItems FOR UPDATE";

            String queryFour = "INSERT INTO bingoCardItems (bingo_card_id, itemNumber, item_id, game_id) VALUES " + ItemManager.argumentsBuilderDatabaseQuery(4, 25);

            Connection dbConn = DatabaseManager.getConnection();

            // Set connection in "transaction mode"
            dbConn.setAutoCommit(false);

            PreparedStatement stmtThree = dbConn.prepareStatement(queryThree);
            ResultSet resultSet = stmtThree.executeQuery();

            int nextCardId = 0;

            if(resultSet.next()) {
                nextCardId = resultSet.getInt("next_id");
                bingoBoard.setBingoCardID(nextCardId);
            }

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

            // Create bingo card and set argument counter
            int counter = 1;
            // Create item counter
            int itemCounter = 0;

            PreparedStatement stmtFour = dbConn.prepareStatement(queryFour);

            for (BingoItem item : bingoBoard.getItems()) {
                // Set bingo card id
                stmtFour.setInt(counter, nextCardId);
                counter++;

                // Set item number
                stmtFour.setInt(counter, itemCounter);
                counter++;
                itemCounter++;

                //  Set Item ID
                stmtFour.setInt(counter, item.getItemID());
                counter++;

                // set Game ID
                stmtFour.setInt(counter, this.gameID);
                counter++;
            }

            stmtFour.executeUpdate();

            // Set player in game
            PreparedStatement stmtTwo = dbConn.prepareStatement(queryTwo);

            // Reset argument counter
            counter = 1;

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
