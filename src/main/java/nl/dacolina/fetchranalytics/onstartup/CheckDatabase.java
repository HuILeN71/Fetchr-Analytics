package nl.dacolina.fetchranalytics.onstartup;

import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;

public class CheckDatabase {

    public static boolean areAllTablesAvailable(String databaseName) {
        try {

            FetchrAnalytics.LOGGER.info("Test");

            Connection dbConn = DatabaseManager.getConnection();

            String query = queryConstructerHelper(databaseName);

            FetchrAnalytics.LOGGER.info(query);

            PreparedStatement stmt = dbConn.prepareStatement(query);

            stmt.setString(1, databaseName);

            int j = 2;

            String[] tables = Config.getDatabaseTables();

            ArrayList<String> foundTables = new ArrayList<>();

            for (int i = 0; i < tables.length; i++) {
                stmt.setString(j, tables[i]);
                j++;
            }

            ResultSet rs = stmt.executeQuery();

            // Get metadata to know the number of columns
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Iterate over the ResultSet and build the output
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    for (int k = 0; k < tables.length; k++) {
                        if(rs.getString(i).equals(tables[k])) {
                            foundTables.add(tables[k]);
                        }
                    }
                }
            }

            FetchrAnalytics.LOGGER.info(String.valueOf(foundTables));

            if (tables.length == foundTables.size()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean ableToConnect() {

        try {
            DatabaseManager.getConnection();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String queryConstructerHelper(String databaseName) {

        String[] tables = Config.getDatabaseTables();

        StringBuilder query = new StringBuilder("SELECT TABLE_NAME FROM information_schema.TABLES WHERE table_schema = ? AND TABLE_NAME IN (");

        for (int i = 0; i < tables.length; i++) {
            if(i != tables.length - 1) {
                query.append("?, ");
            } else {
                query.append("?");
            }

        }

        query.append(");");

        return String.valueOf(query);

    }

    public static boolean constructDatabase() {

        return false;
    }

}
