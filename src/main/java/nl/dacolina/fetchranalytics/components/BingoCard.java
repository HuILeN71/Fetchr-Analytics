package nl.dacolina.fetchranalytics.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BingoCard {
    private static final int DEFAULT_ITEM_AMOUNT = 25;


    private List<BingoItem> items;
    private int bingoCardID;


    public BingoCard(MinecraftServer server) {

        this.items = getItemsFromCard(server);

        // Set the ID's from the items
        setItemIDs();

        this.bingoCardID = 0;
        // debugShowCard(items);

    }

    public BingoCard(int gameID) {
        this.items = restoreItemsFromDatabase(gameID);

        this.bingoCardID = getCurrentBingoCardID(gameID);
    }

    private int getCurrentBingoCardID(int gameID) {
        String query = "SELECT bci.bingo_card_id FROM bingoCardItems bci WHERE bci.game_id = ? LIMIT 1";

        int bingoBoardID = 0;

        try {
            Connection dbConn = DatabaseManager.getConnection();

            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setInt(1, gameID);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                bingoBoardID =  rs.getInt("bingo_card_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bingoBoardID;
    }

    private List<BingoItem> restoreItemsFromDatabase(int gameID) {
        // Init list for bingo items
        List<BingoItem> bingoCardDatabase = new ArrayList<>();

        // Set query
        String query = "SELECT bci.bingo_card_id, bci.itemNumber, bci.item_id, i.mc_id, i.components FROM bingoCardItems bci INNER JOIN items i ON bci.item_id = i.item_id WHERE bci.game_id = ? ORDER BY bingo_card_id, itemNumber";

        // Fetch items from database (id, mc_id, component)
        try {
            Connection dbConn = DatabaseManager.getConnection();

            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setInt(1, gameID);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                BingoItem item = new BingoItem(rs.getString("mc_id"), rs.getString("components"));
                // Set item id now, since it is already here
                item.setItemID(rs.getInt("item_id"));
                bingoCardDatabase.add(item);
            }

            dbConn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bingoCardDatabase;

    }

    private void setItemIDs() {
        // Init query string
        StringBuilder query = new StringBuilder("SELECT item_id, mc_id, components FROM items WHERE ");
        boolean isFirst = true;

        // Construct query to get all the id's from the necessary items
        for (BingoItem item : this.items) {
            //Always add item name to query

            if(isFirst) {
                query.append("(mc_id = ?");
                isFirst = false;
            } else {
                query.append(" OR (mc_id = ?");
            }

            // If component is not null add components value
            if(item.getComponent() != null) {
                query.append(" AND components = ?)");
            } else {
                query.append(")");
            }

        }

        // Create counter
        int counter = 1;

        try {
            // Create connection and create prepared query
            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query.toString());

            // set all values
            for (BingoItem item : this.items) {
                // Item name and increase counter
                stmt.setString(counter, item.getMinecraftItemName());
                counter++;

                if(item.getComponent() != null) {
                    // Set component and increase counter
                    stmt.setString(counter, item.getComponent());
                    counter++;
                }
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FetchrAnalytics.LOGGER.debug(rs.getInt("item_id") + " -- " + rs.getString("mc_id") + " -- " + rs.getString("components"));

                for(BingoItem item : items) {
                    if(Objects.equals(item.getMinecraftItemName(), rs.getString("mc_id")) && Objects.equals(item.getComponent(), rs.getString("components"))) {
                        item.setItemID(rs.getInt("item_id"));
                    }
                }
            }

            dbConn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // debugItemInformation(items);

        FetchrAnalytics.LOGGER.debug(query.toString());

    }

    private static List<BingoItem> getItemsFromCard(MinecraftServer server) {

        List<BingoItem> listOfItems = new ArrayList<>();

        //Load the items storage
        Identifier storageId = Identifier.of("fetchr", "card");

        NbtCompound nbt = server.getDataCommandStorage().get(storageId);

        NbtList cardItems = nbt.getList("slots", NbtElement.COMPOUND_TYPE);

        for(int i = 0; i < DEFAULT_ITEM_AMOUNT; i++) {
            NbtCompound cardItem = cardItems.getCompound(i);

            NbtCompound item = cardItem.getCompound("item");

            NbtCompound components = item.getCompound("components");

            String itemId = item.getString("id");

            // Check if item has components

            // Add checks to see if items have these compounds, otherwise delete them. !YET TO ADD
            components.remove("minecraft:custom_name");
            components.remove("minecraft:lore");

            BingoItem newItem = null;

            if (item.contains("components", NbtElement.COMPOUND_TYPE) && !components.isEmpty()) {

                if(cardItem.contains("item_tests", NbtElement.LIST_TYPE)) {
                    NbtList itemTests = cardItem.getList("item_tests", NbtElement.COMPOUND_TYPE);

                    NbtCompound normalizedActiveTests = itemTests.getCompound(0);

                    String checkedComponent = normalizedActiveTests.getString("id");

                    if (components.contains(checkedComponent)) {

                        // Parts of the item checks
                        NbtCompound partsOfItemChecks = normalizedActiveTests.getCompound("predicate");

                        // Check if armor trim pattern is being checked. Manual for now, dont yet know how to implement
                        // a way to make this completely dynamic. For now manual controls will be added to this section.

                        FetchrAnalytics.LOGGER.debug(checkedComponent);

                        String engeString = "{\"" + checkedComponent + "\":" + partsOfItemChecks + "}";

//                        if(Objects.equals(checkedComponent, "minecraft:trim") && !partsOfItemChecks.contains("pattern")) {
//                            // Add any value so that database knows the difference between this item and one where a
//                            // certain trim is required. Also used to determine display name.
//
//                            partsOfItemChecks.putString("pattern", "minecraft:any");
//
//                        }

                        newItem = new BingoItem(itemId, engeString);

                    }

                } else {
                    newItem = new BingoItem(itemId, components.toString());
                }

            } else {
                newItem = new BingoItem(itemId, null);
            }

            listOfItems.add(newItem);
        }
        return listOfItems;
    }

    public void debugShowCard(List<Item> items) {
        for (Item itemFromList : items) {

            StringBuilder s = new StringBuilder();

            s.append(itemFromList.getMinecraftItemName());

            if(itemFromList.getComponent() != null) {
                s.append(itemFromList.getComponent());
            }

            FetchrAnalytics.LOGGER.info(s.toString());

        }
    }

    public List<BingoItem> getItems() {
        return this.items;
    }

    public void setBingoCardID(int cardID) {
        this.bingoCardID = cardID;
    }

    public int getBingoCardID() {
        return this.bingoCardID;
    }

}


