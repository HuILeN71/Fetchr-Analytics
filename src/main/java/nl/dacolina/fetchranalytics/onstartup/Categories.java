package nl.dacolina.fetchranalytics.onstartup;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import javax.sound.midi.Soundbank;
import java.sql.*;
import java.util.ArrayList;

public class Categories {

    private static String[] getCategoriesFromGame(MinecraftServer server) {

        Identifier storageId = Identifier.of("fetchr", "registries");

        NbtCompound nbt = server.getDataCommandStorage().get(storageId);

        NbtList categories = nbt.getList("categories", NbtElement.COMPOUND_TYPE);

        FetchrAnalytics.LOGGER.info("Amount of categories present in game: " + String.valueOf(categories.size()));

        // Create an array the size of the amount of categories currently in game
        String[] categoriesInGame = new String[categories.size()];

        for (int i = 0; i < categories.size(); i++) {
            NbtCompound category = categories.getCompound(i);
            String categoryId = category.getString("id");
            categoriesInGame[i] = categoryId;

            //Debug
            //System.out.println(categoryId + " - " + getCategoryDisplayName(categoryId));

        }

        return categoriesInGame;
    }

    private static ArrayList<String> getCategoriesFromDatabase() {

        ArrayList<String> existingCategories = new ArrayList<String>();

        try {
            Connection dbConn = DatabaseManager.getConnection();

            String query = "SELECT fetchr_category_id FROM categories";

            PreparedStatement stmt = dbConn.prepareStatement(query);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                existingCategories.add(rs.getString(1));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        // System.out.println(existingCategories);

        return existingCategories;

    }

    private static String getCategoryDisplayName(String fetchrCategory) {

        String displayName = null;

        String[] parts = fetchrCategory.split(":");

        displayName = parts[1].replace("_", " ");

        displayName = displayName.substring(0,1).toUpperCase() + displayName.substring(1);

        return displayName;

    }

    public static boolean addMissingCategoriesToDatabase(ArrayList<String> categories) {


        try {
            Connection dbConn = DatabaseManager.getConnection();

            // Create query

            StringBuilder query = new StringBuilder("INSERT INTO categories (fetchr_category_id , displayName) VALUES ");

            int counter = 0;

            for (String category : categories) {
                counter++;

                if(counter != categories.size()) {
                    query.append("(?, ?),");
                } else {
                    query.append("(?, ?);");
                }

            }
            System.out.println(query);
            System.out.println(counter);

            // Prepare query

            PreparedStatement stmt = dbConn.prepareStatement(String.valueOf(query));

            // Fill all the question marks

            int j = 1;

            for (String category : categories) {
                stmt.setString(j, category);

                j++;

                stmt.setString(j, getCategoryDisplayName(category));

                j++;
            }

            stmt.execute();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;


    }


    public static ArrayList<String> getMissingCategories(MinecraftServer server) {


        // Load all the current categories in memory
        String[] categoriesFromGame = getCategoriesFromGame(server);

        // Load all categories already in the database into memory
        ArrayList<String> categoriesFromDatabase = getCategoriesFromDatabase();

        ArrayList<String> missingCategories = new ArrayList<String>();

        for (int i = 0; i < categoriesFromGame.length; i++) {
            if(!categoriesFromDatabase.contains(categoriesFromGame[i])) {
                // If database does not contain the category it will be added
                missingCategories.add(categoriesFromGame[i]);
            }
        }

        return missingCategories;


    }



}
