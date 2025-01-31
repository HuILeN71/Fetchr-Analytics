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
    private static final String GAMEMODE_LINE = "Line";
    private static final String TAG_REGEX = "fetchranalytics.has_slot.";
    private static final String GAMEMODE_BLACK_OUT = "Black_out";

    private int gameID;
    private int playerCount;
    private int teamCount;
    private boolean isInitialized;
    private List<Team> teams;
    private int seed;
    private BingoCard bingoBoard;

    public Game(MinecraftServer server, boolean isRestart) {

        this.isInitialized = false;

        if(!isRestart) {
            // Set init value to false

            this.playerCount = 0;
            this.teamCount = 0;
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

        } else {
            // First get last game from database so that everything can be restored.
            int[] necessaryDataFromGamesTable = getDataFromGameTable(1);

            // Set gotten values
            this.gameID = necessaryDataFromGamesTable[0];
            this.seed = necessaryDataFromGamesTable[1];

            // Init the bingo board from database
            this.bingoBoard = new BingoCard(this.gameID);

            // Restore the teams and their players (With their items) from the database
            this.teams = restoreTeamsFromDatabase(this.gameID);


        }

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

                     //FetchrAnalytics.LOGGER.info("Player being checked: " + teamMember.getPlayerName() + ", stored UUID: " + teamMember.getPlayerUUID() + ", game UUID: " + player.getUuid());

                     if(player.getUuid().equals(teamMember.getPlayerUUID())) {

                         checkForTags(player, teamMember, team, timeInGame, server);

                         // Check distance
                         teamMember.calculateDistance(player.getBlockX(), player.getBlockZ());

                         FetchrAnalytics.LOGGER.info("Player walked " + teamMember.getDistanceWalked() + " meters");

                     }
                 }
             }
         }
    }

    private int checkForTags(ServerPlayerEntity player, Player teamMember, Team team, String timeInGame, MinecraftServer server) {
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

                        team.addItemToVirtualBoard(itemNumber);

                        if(!team.isLineBingo()) {
                            String bingoLocation = team.checkForLineBingo();

                            FetchrAnalytics.LOGGER.debug(String.valueOf(team.isLineBingo()));
                            FetchrAnalytics.LOGGER.debug(bingoLocation);

                            if(!Objects.equals(bingoLocation, "none")) {
                                FetchrAnalytics.LOGGER.debug("Entry will be updated in database!");
                                team.setLineBingo(true);
                                updateGamemodeInDatabase(GAMEMODE_LINE, team.getTeamName(), bingoLocation);

                            }

                        }

                        FetchrAnalytics.LOGGER.info(String.valueOf(team.getItemCount()));

                        if(team.getItemCount() == 25) {
                            team.setBlackOut(true);
                            updateGamemodeInDatabase(GAMEMODE_BLACK_OUT, team.getTeamName(), "none");
                        }

                    }
                    // FetchrAnalytics.LOGGER.info(tag);

                }


            }
        }

        return -1;

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
            this.teamCount++;
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

            String queryTwo = "INSERT INTO teamInGame (game_id, teams_id, gameMode) VALUES " + ItemManager.argumentsBuilderDatabaseQuery(3, this.teamCount);

            String queryThree = "SELECT COALESCE(MAX(bingo_card_id), 0) + 1 AS next_id FROM bingoCardItems FOR UPDATE";

            String queryFour = "INSERT INTO bingoCardItems (bingo_card_id, itemNumber, item_id, game_id) VALUES " + ItemManager.argumentsBuilderDatabaseQuery(4, 25);

            String queryFive = "INSERT INTO playerInGame (game_id, teams_id, player_id) VALUES " + ItemManager.argumentsBuilderDatabaseQuery(3, this.playerCount);

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

            // Set teams in game
            PreparedStatement stmtTwo = dbConn.prepareStatement(queryTwo);

            // Reset argument counter
            counter = 1;

            for (Team team : teamsInGame) {
                    // Set game id
                stmtTwo.setInt(counter, gameID);
                counter++;

                // Set team name
                stmtTwo.setString(counter, team.getTeamName());
                counter++;

                // Set team member
                stmtTwo.setString(counter, "none");
                counter++;

            }

            stmtTwo.executeUpdate();

            // Set players in game
            PreparedStatement stmtFive = dbConn.prepareStatement(queryFive);

            counter = 1;

            for (Team team : teamsInGame) {
                for(Player teamMember : team.getTeamMembers()) {
                    // Set game id
                    stmtFive.setInt(counter, this.gameID);
                    counter++;

                    // Set team name
                    stmtFive.setString(counter, team.getTeamName());
                    counter++;

                    // Set team member
                    stmtFive.setString(counter, String.valueOf( teamMember.getPlayerUUID()));
                    counter++;

                }
            }

            stmtFive.executeUpdate();

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

    private void updateGamemodeInDatabase(String gameMode, String teamName, String lineType) {

        String query = "UPDATE teamInGame SET gameMode = ?, lineType = ? WHERE game_id = ? AND teams_id = ?";

        try {
            Connection dbConn = DatabaseManager.getConnection();

            PreparedStatement stmt = dbConn.prepareStatement(query);

            // Set gameMode
            stmt.setString(1, gameMode);

            // Set linetype
            stmt.setString(2, lineType);

            // Set gameid
            stmt.setInt(3, this.gameID);

            // Set team id
            stmt.setString(4, teamName);

            stmt.execute();

            dbConn.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private int[] getDataFromGameTable(int serverId) {
        String query = "SELECT g.game_id, g.seed FROM game g WHERE server_id = ? ORDER BY game_id DESC LIMIT 1";

        int[] gameData = new int[2];

        try {
            Connection dbConn = DatabaseManager.getConnection();

            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setInt(1, serverId);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                gameData[0] = rs.getInt("game_id");
                gameData[1] = rs.getInt("seed");
            }

            dbConn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gameData;

    }

    private List<Team> restoreTeamsFromDatabase(int gameID) {

        // Team tracker map
        Map<String,Boolean> existingTeams = new HashMap<>();

        // Init teams list
        List<Team> teamsFromDatabase = new ArrayList<>();

        // Get query ready
        String query = "SELECT tig.teams_id, tig.gameMode, tig.lineType, pig.player_id, p.displayName, distanceWalked FROM teamInGame tig INNER JOIN playerInGame pig ON tig.teams_id = pig.teams_id AND tig.game_id = pig.game_id INNER JOIN players p ON pig.player_id  = p.mc_uuid WHERE tig.game_id = ?";

        // Get teams and players from current game ID and create the teams with their corresponding players
        try {
            Connection dbConn = DatabaseManager.getConnection();

            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setInt(1, gameID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String teamName = rs.getString("teams_id");

                if(!existingTeams.containsKey(teamName)) {

                    existingTeams.put(teamName, true);
                    Team team = new Team(teamName);

                    String gameMode = rs.getString("gameMode");
                    String lineType = rs.getString("lineType");

                    if(!Objects.equals(lineType, "none")) {
                        team.setLineBingo(true);
                    }

                    if(Objects.equals(lineType, "Black_out")) {
                        team.setBlackOut(true);
                    }

                    teamsFromDatabase.add(team);
                    this.teamCount++;

                }

                UUID currentPlayerID = UUID.fromString(rs.getString("player_id"));
                String displayName = rs.getString("displayName");


                for(Team team : teamsFromDatabase) {
                    if(Objects.equals(team.getTeamName(), teamName)) {
                        team.addTeamMember(currentPlayerID, displayName);

                        this.playerCount++;

                        for(Player player : team.getTeamMembers()) {
                            if(player.getPlayerUUID() == currentPlayerID) {
                                player.setDistanceWalked(rs.getInt("distanceWalked"));
                                // Init all the items that the player has gotten

                                String queryTwo = "SELECT bci.itemNumber, bci.item_id, iig.player_id, iig.timeGotten FROM bingoCardItems bci INNER JOIN itemsInGame iig ON bci.bingo_card_id = iig.bingo_card_id AND bci.itemNumber = iig.itemNumber WHERE bci.game_id  = ? AND player_id = ?";

                                PreparedStatement stmtTwo = dbConn.prepareStatement(queryTwo);

                                stmtTwo.setInt(1, gameID);
                                stmtTwo.setString(2, String.valueOf(player.getPlayerUUID()));

                                ResultSet rsTwo = stmtTwo.executeQuery();

                                while(rsTwo.next()) {
                                    player.addCollectedItem(rsTwo.getInt("itemNumber"), rsTwo.getString("timeGotten"));
                                }
                            }
                        }
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teamsFromDatabase;

    }


}
