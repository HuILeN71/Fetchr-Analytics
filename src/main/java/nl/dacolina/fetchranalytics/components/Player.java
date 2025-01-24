package nl.dacolina.fetchranalytics.components;

import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.*;
import java.util.*;

public class Player {

    private List<Map<Integer, Time>> collectedItems;
    private UUID playerUUID;
    private String playerName;
    private int lastCountOfTags;

    public Player(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.collectedItems = new ArrayList<>();
        this.lastCountOfTags = 0;
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

    public void addCollectedItem(int slotNumber, Time timeCollected) {
        Map<Integer, Time> newCollectedItem = new HashMap<>();
        newCollectedItem.put(slotNumber, timeCollected);
        collectedItems.add(newCollectedItem);
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
}
