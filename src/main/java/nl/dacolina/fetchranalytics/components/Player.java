package nl.dacolina.fetchranalytics.components;

import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.*;
import java.util.*;

import static java.lang.Math.sqrt;

public class Player {

    private Map<Integer, Map<Integer, String>> collectedItems;
    private UUID playerUUID;
    private String playerName;
    private int lastCountOfTags;
    private int previousX;
    private int previousZ;
    private int distanceWalked;
    private int lastDistanceUpdatedToDatabase;
    private int playerTickCounter;

    public Player(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.collectedItems = new HashMap<>();
        this.lastCountOfTags = 0;
        this.previousX = 0;
        this.previousZ = 0;
        this.distanceWalked = 0;
        this.lastDistanceUpdatedToDatabase = 0;
        this.playerTickCounter = 1200;

    }

    public String getPlayerName() {
        return this.playerName;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setLastCountOfTags(int count) {
        this.lastCountOfTags = count;
    }

    public int getLastCountOfTags() {
        return this.lastCountOfTags;
    }

    public void addCollectedItem(int slotNumber, String timeCollected) {
        Map<Integer, String> newCollectedItem = new HashMap<>();
        newCollectedItem.put(slotNumber, timeCollected);
        collectedItems.put(slotNumber, newCollectedItem);
    }

    public Map<Integer, Map<Integer, String>> getCollectedItems() {
        return this.collectedItems;
    }

    public void checkForPlayerDatabaseEntry(UUID playerUUID, String playerName) {

        String query = "SELECT mc_uuid, displayName FROM players WHERE mc_uuid = ?";

        try {
            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setString(1, String.valueOf(playerUUID));

            ResultSet rs = stmt.executeQuery();

            // Do the following if the record does not exist
            if(rs.next()) {

                if(!Objects.equals(rs.getString("displayName"), playerName)) {
                    // Player name has changed! Update entry in database
                    query = "UPDATE players SET displayName = ? WHERE mc_uuid = ?";

                    stmt = dbConn.prepareStatement(query);

                    stmt.setString(1, playerName);
                    stmt.setString(2, String.valueOf(playerUUID));

                    stmt.execute();
                }
            } else {
                // Player does not yet exist in database. Add new entry
                query = "INSERT INTO players (mc_uuid, displayName) VALUES (?, ?)";

                stmt = dbConn.prepareStatement(query);

                stmt.setString(1, String.valueOf(playerUUID));
                stmt.setString(2, playerName);

                stmt.execute();

            }

            dbConn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void calculateDistance(int newX, int newZ, int gameID) {
        if (this.previousX != 0 && this.previousZ != 0) {
            this.distanceWalked += (int) sqrt(((newX - this.previousX) * (newX - this.previousX)) + ((newZ - this.previousZ) * (newZ - this.previousZ)));
        }

        if (this.playerTickCounter != 0 && this.distanceWalked > this.lastDistanceUpdatedToDatabase) {
            this.playerTickCounter--;
        } else {
            this.playerTickCounter = 1200;
            this.lastDistanceUpdatedToDatabase = this.distanceWalked;
            updateDistanceInDatabase(gameID);
        }

        this.previousX = newX;
        this.previousZ = newZ;
    }

    public void updateDistanceInDatabase(int gameID) {

        String query = "UPDATE playerInGame SET distanceWalked = ? WHERE game_id = ? AND player_id = ?";

        try {
            Connection dbConn = DatabaseManager.getConnection();

            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setInt(1, this.distanceWalked);

            stmt.setInt(2, gameID);

            stmt.setString(3, String.valueOf(this.playerUUID));

            stmt.execute();

            dbConn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public int getDistanceWalked() {
        return distanceWalked;
    }

    public void setDistanceWalked(int distance) {
        this.distanceWalked = distance;
    }
}
