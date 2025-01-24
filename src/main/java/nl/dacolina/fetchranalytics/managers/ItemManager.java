package nl.dacolina.fetchranalytics.managers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.components.Category;
import nl.dacolina.fetchranalytics.components.FullItem;
import nl.dacolina.fetchranalytics.components.Item;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class ItemManager {

    private static final int TEMP_SERVER_ID = 1;
    private static final int AMOUNT_COLUMNS_ITEM_TABLE = 4;
    private static final int AMOUNT_COLUMNS_HISTORY_TABLE = 4;

    private List<FullItem> itemsCurrentlyInGame;
    private List<FullItem> itemsCurrentlyInDatabase;

    public ItemManager(MinecraftServer server) {

        this.itemsCurrentlyInGame = getItemsLoadedInGame(server);

        // Fix later, hopefully you read this later. This is probably unnecessary, but I do not know a fix. Thank you
        List<FullItem> itemsBackup = getItemsLoadedInGame(server);

        //FetchrAnalytics.LOGGER.info(this.itemsCurrentlyInGame.toString());
        // This function is going to retrieve information from the database to see weather an item is in the database or not
        createMissingItemsInDatabase(itemsBackup);

        // This function is going to add all the category information to the database.
        createMissingCategoriesInHistory(this.itemsCurrentlyInGame, TEMP_SERVER_ID);


    }

    private static String getNameWithoutMinecraftPrefix(String fullName) {
        String[] temp = fullName.split(":");

        return temp[1];

    }

    private static List<FullItem> getItemsLoadedInGame(MinecraftServer server) {

        List<FullItem> listOfItems = new ArrayList<>();

        //Load the items storage
        Identifier storageId = Identifier.of("fetchr", "items");

        NbtCompound nbt = server.getDataCommandStorage().get(storageId);

        NbtList activeItems = nbt.getList("active_items", NbtElement.COMPOUND_TYPE);

        //FetchrAnalytics.LOGGER.info(String.valueOf(activeItems.size()));

        for(int i = 0; i < activeItems.size(); i++) {
            NbtCompound activeItem = activeItems.getCompound(i);

            NbtCompound item = activeItem.getCompound("item");

            NbtList categories = activeItem.getList("categories", NbtElement.COMPOUND_TYPE);

            NbtCompound components = item.getCompound("components");

            String itemId = item.getString("id");

            // Check if item has components

            // Add checks to see if items have these compounds, otherwise delete them. !YET TO ADD
            components.remove("minecraft:custom_name");
            components.remove("minecraft:lore");

            FullItem newItem = null;

            if (components != null && item.contains("components", NbtElement.COMPOUND_TYPE) && !components.isEmpty()) {

                if(activeItem.contains("item_tests", NbtElement.LIST_TYPE)) {
                    NbtList itemTests = activeItem.getList("item_tests", NbtElement.COMPOUND_TYPE);

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

                        newItem = new FullItem(itemId, engeString);

                    }

                } else {
                    newItem = new FullItem(itemId, components.toString());
                }

            } else {
                newItem = new FullItem(itemId, null);
            }

            for (int j = 0; j < categories.size(); j++) {
                NbtCompound currentCategory = categories.getCompound(j);

                String categoryName = currentCategory.getString("id");

                int categoryWeight = currentCategory.getInt("weight");

                newItem.addCategoryToItem(categoryName, categoryWeight);

                //FetchrAnalytics.LOGGER.info(itemId + " -- " + categoryName + " -- " + categoryWeight);

            }

            listOfItems.add(newItem);


        }

        return listOfItems;
    }

    // This function retrieves simple item data, so that items can be compared and if it needs to be added to the database.
    private static List<Item> getSimpleItemsFromDatabase() {

        List<Item> itemsFromDatabase = new ArrayList<>();

        // Set query
        String query = "SELECT mc_id, components FROM items";

        try {

            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                itemsFromDatabase.add(new Item(rs.getString("mc_id"), rs.getString("components")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itemsFromDatabase;
    }


    private static void createMissingItemsInDatabase(List<FullItem> items) {

        List<Item> itemsAlreadyInDatabase = getSimpleItemsFromDatabase();

        Iterator<FullItem> iterator = items.iterator();

        while (iterator.hasNext()) {

            FullItem item = iterator.next();

            for (Item itemInDB : itemsAlreadyInDatabase) {
                if (Objects.equals(itemInDB.getMinecraftItemName(), item.getMinecraftItemName()) && Objects.equals(itemInDB.getComponent(), item.getComponent())) {
                    iterator.remove();
                }
            }
        }

        if(!items.isEmpty()) {
            createItemsInDatabase(items);
        }


    }

    private static void createItemsInDatabase (List<FullItem> itemsToAdd) {
        String queryArguments = argumentsBuilderDatabaseQuery(AMOUNT_COLUMNS_ITEM_TABLE, itemsToAdd.size());
        FetchrAnalytics.LOGGER.info(queryArguments);

        // Create full query
        String query = "INSERT INTO items (mc_id, components, displayName, availableOnDisk) VALUES " + queryArguments;

        try {
            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query);

            // Loop through items and set values and set counter
            int counter = 1;

            for (FullItem item : itemsToAdd) {
                // MC_ID
                stmt.setString(counter, item.getMinecraftItemName());

                // Increase counter
                counter++;

                // Set null on the component when none is provided
                FetchrAnalytics.LOGGER.debug(item.getComponent());

                if (item.getComponent() != null) {
                    stmt.setString(counter, item.getComponent());
                } else {
                    stmt.setNull(counter, Types.VARCHAR);
                }

                // Increase counter
                counter++;

                // Set display name
                stmt.setString(counter, item.getDisplayName());

                // Increase counter
                counter++;

                // Set storagebit
                stmt.setBoolean(counter, false);

                // Increase counter
                counter++;
            }

            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static List<FullItem> setAllItemIDsHelper(List<FullItem> items) {

        // Init query string
        StringBuilder query = new StringBuilder("SELECT item_id, mc_id, components FROM items WHERE ");
        boolean isFirst = true;

        // Construct query to get all the id's from the necessary items
        for (FullItem item : items) {
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
            for (FullItem item : items) {
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

                for(FullItem item : items) {
                    if(Objects.equals(item.getMinecraftItemName(), rs.getString("mc_id")) && Objects.equals(item.getComponent(), rs.getString("components"))) {
                        item.setDatabaseItemID(rs.getInt("item_id"));
                    }
                }
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }

        // debugItemInformation(items);

        FetchrAnalytics.LOGGER.debug(query.toString());

        return items;

    }

    private static int categoryCounterHelper (List<FullItem> items) {

        int totalCategoryCount = 0;

        for (FullItem item : items) {

            List<Category> categories = item.getCategories();

            totalCategoryCount += categories.size();
        }

        return totalCategoryCount;
    }

    private static ResultSet getCurrentActiveCategoriesInDatabase() {

        String query = "SELECT items.item_id, items.mc_id, items.components, history_id, fetchr_category_id, itemWeightCategory, endDate " +
                "FROM itemHistoryInCategory INNER JOIN items ON itemHistoryInCategory.item_id = items.item_id WHERE server_id = 1 AND endDate IS NULL";

        try {
            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query);

            return stmt.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void disableCategories(List<Map<String, Object>> databaseRows) {

        String baseQuery = "UPDATE itemHistoryInCategory SET endDate = NOW() WHERE history_id = (?)";

        try {

            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(baseQuery);

            for (Map<String, Object> row : databaseRows) {

                stmt.setInt(1, (int) row.get("history_id"));
                stmt.addBatch();
            }

            stmt.executeBatch();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void createMissingCategoriesInHistory(List<FullItem> items, int serverID) {

        // Items in the game currently
        List<FullItem> updatedItems = setAllItemIDsHelper(items);

        // Items with categories from the current server that are active in the database
        ResultSet rs = getCurrentActiveCategoriesInDatabase();

        // Convert ResultSet into a list of maps (activeDatabaseItems)
        List<Map<String, Object>> activeDatabaseItems = new ArrayList<>();
        try {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("item_id", rs.getInt("item_id"));
                row.put("history_id", rs.getInt("history_id"));
                row.put("fetchr_category_id", rs.getString("fetchr_category_id"));
                row.put("itemWeightCategory", rs.getInt("itemWeightCategory"));
                activeDatabaseItems.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Process items to manage categories
        List<Map<String, Object>> newCategories = new ArrayList<>();
        List<Map<String, Object>> categoriesToDisable = new ArrayList<>();

        // Track active categories in a map for faster lookup
        Map<String, Map<String, Object>> activeCategoryMap = new HashMap<>();
        for (Map<String, Object> row : activeDatabaseItems) {
            String key = row.get("item_id") + ":" + row.get("fetchr_category_id") + ":" + row.get("itemWeightCategory");
            activeCategoryMap.put(key, row);
        }
        
        // Iterate through items in the game
        for (FullItem item : updatedItems) {

            for (Category category : item.getCategories()) {
                String key = item.getDatabaseItemID() + ":" + category.getCategoryName() + ":" + category.getCategoryWeight();

                if (activeCategoryMap.containsKey(key)) {
                    // Category exists in the database, leave it untouched
                    activeCategoryMap.remove(key);
                } else {
                    // Category is new, add it to the list of new categories
                    Map<String, Object> newCategory = new HashMap<>();
                    newCategory.put("item_id", item.getDatabaseItemID());
                    newCategory.put("fetchr_category_id", category.getCategoryName());
                    newCategory.put("itemWeightCategory", category.getCategoryWeight());

                    FetchrAnalytics.LOGGER.debug(newCategory.toString());

                    newCategories.add(newCategory);
                }
            }
        }

        for (Map<String, Object> row : activeCategoryMap.values()) {
            //row.put("endDate", new Date());

            FetchrAnalytics.LOGGER.debug(row.toString());

            categoriesToDisable.add(row);
        }

        if(!categoriesToDisable.isEmpty()) {
            disableCategories(categoriesToDisable);
        }

        if(!newCategories.isEmpty()) {
            createNewCategoriesInHistoryInDatabase(newCategories, TEMP_SERVER_ID);
        }

    }

    private static void createNewCategoriesInHistoryInDatabase(List<Map<String, Object>> newCategories, int serverID) {
        int rows = newCategories.size();

        String queryArguments = argumentsBuilderDatabaseQuery(AMOUNT_COLUMNS_HISTORY_TABLE, rows);

        String query = "INSERT INTO itemHistoryInCategory (item_id, fetchr_category_id, itemWeightCategory, server_id) VALUES" + queryArguments;

        try {
            Connection dbConn = DatabaseManager.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(query);

            FetchrAnalytics.LOGGER.debug(query);

            // Start counter
            int counter = 1;
            for (Map<String, Object> itemCategory : newCategories) {
                // Count based on category after, because item can have multiple categories!
                // Set Item ID
                stmt.setInt(counter, (int) itemCategory.get("item_id"));
                // Increase counter
                counter++;

                // Set current category
                stmt.setString(counter, (String) itemCategory.get("fetchr_category_id"));
                // Increase counter
                counter++;

                stmt.setInt(counter, (int) itemCategory.get("itemWeightCategory"));
                // Increase counter
                counter++;

                stmt.setInt(counter, serverID);
                // Increase counter
                counter++;

            }

            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String argumentsBuilderDatabaseQuery(int amountOfColumns, int amountOfRows) {
        StringBuilder arguments = new StringBuilder();

        for (int i = 0; i < amountOfRows; i++) {

            if(i == 0) {
                arguments.append("(");
            } else {
                arguments.append(", (");
            }

            for (int j = 0; j < amountOfColumns; j++) {

                if(amountOfColumns - 1 != j) {
                    arguments.append("?, ");
                } else {
                    arguments.append("?");
                }

            }

            arguments.append(")");
        }

        return arguments.toString();

    }

    private static void debugItemInformation(List<FullItem> items) {
        for (FullItem itemFromList : items) {

            StringBuilder s = new StringBuilder();

            if(itemFromList.getDatabaseItemID() != 0) {
                s.append("id: " + itemFromList.getDatabaseItemID() + " -- ");
            }

            s.append(itemFromList.getMinecraftItemName());

            if(itemFromList.getComponent() != null) {
                s.append(itemFromList.getComponent());
            }

            s.append(" aka. " + itemFromList.getDisplayName() + " - ");

            for (Category category : itemFromList.getCategories()) {
                s.append(category.getCategoryName() + ", " + category.getCategoryWeight() + " | ");
            }

            FetchrAnalytics.LOGGER.info(s.toString());

        }
    }
}

