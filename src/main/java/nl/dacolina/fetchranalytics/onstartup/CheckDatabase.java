package nl.dacolina.fetchranalytics.onstartup;

import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;

public class CheckDatabase {
    private static final String DB_CREATION_SCRIPT_GAME = """
            CREATE TABLE IF NOT EXISTS game (
                game_id int NOT NULL AUTO_INCREMENT,
                startDate DATE NOT NULL,
                startTime TIME NOT NULL,
                endDate DATE NOT NULL,
                endTime TIME NOT NULL,
                seed int NOT NULL,
                isRow boolean NOT NULL,
                endTimeRow int NOT NULL,
                PRIMARY KEY (game_id)
            )
            """;

    private static final String DB_CREATION_SCRIPT_TEAMS= """
            CREATE TABLE IF NOT EXISTS teams (
                fetchr_team_id varchar(200) NOT NULL,
                displayName varchar(255) NOT NULL,
                PRIMARY KEY (fetchr_team_id)
            )
            """;

    private static final String DB_CREATION_SCRIPT_PLAYERS = """
            CREATE TABLE IF NOT EXISTS players (
                mc_uuid char(36) NOT NULL,
                displayName varchar (20) DEFAULT 'Steve',
                headFileLocation varchar(255) NULL,
                availableOnDisk boolean NOT NULL,
                PRIMARY KEY (mc_uuid)
            )
            """;

    private static final String DB_CREATION_SCRIPT_TEAMINGAME = """
            CREATE TABLE IF NOT EXISTS teamInGame (
                game_id int NOT NULL,
                teams_id varchar(200) NOT NULL, -- Change according to above comment
                player_id char(36) NOT NULL,
                UNIQUE (game_id, teams_id, player_id),
                CONSTRAINT FK_game_id_game FOREIGN KEY (game_id)
                REFERENCES game(game_id) ON DELETE CASCADE ON UPDATE CASCADE,
                CONSTRAINT FK_teams_id_teams FOREIGN KEY (teams_id)
                REFERENCES teams(fetchr_team_id),
                CONSTRAINT FK_player_id_players FOREIGN KEY (player_id)
                REFERENCES players(mc_uuid)
            )
            """;

    private static final String DB_CREATION_SCRIPT_CATEGORIES = """
            CREATE TABLE IF NOT EXISTS categories (
                fetchr_category_id varchar(100) NOT NULL,
                displayName varchar(100) DEFAULT 'Placeholder Category',
                PRIMARY KEY (fetchr_category_id)
            )
            """;

    private static final String DB_CREATION_SCRIPT_ITEMS = """
            CREATE TABLE IF NOT EXISTS items (
                mc_id varchar(100) NOT NULL,
                displayName varchar(100) DEFAULT 'Red Bed',
                itemFileLocation varchar(255) NULL,
                availableOnDisk boolean NOT NULL,
                PRIMARY KEY (mc_id)
            )
            """;

    private static final String DB_CREATION_SCRIPT_ITEMHISTORYINCATEGORY = """
            CREATE TABLE IF NOT EXISTS itemHistoryInCategory (
                history_id int NOT NULL AUTO_INCREMENT,
                mc_id varchar(100) NOT NULL,
                fetchr_category_id varchar(100) NOT NULL,
                itemWeightCategory tinyint DEFAULT 1,
                startDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                endDate TIMESTAMP NULL,
                PRIMARY KEY (history_id),
                CONSTRAINT FK_mc_id_itemInCategory_items FOREIGN KEY (mc_id)
                REFERENCES items(mc_id),
                CONSTRAINT FK_fetchrCategory_itemInCategory_categories FOREIGN KEY (fetchr_category_id)
                REFERENCES categories(fetchr_category_id)
            )
            """;

    private static final String DB_CREATION_SCRIPT_ITEMSINGAME = """
            CREATE TABLE IF NOT EXISTS itemsInGame (
                item_id varchar(100) NOT NULL,
                game_id int NOT NULL,
                player_id char(36) NOT NULL,
                timeGotten TIME NOT NULL,
                CONSTRAINT FK_item_id_itemsInGame_items FOREIGN KEY (item_id)
                REFERENCES items(mc_id),
                CONSTRAINT FK_game_id_itemsInGame_game FOREIGN KEY (game_id)
                REFERENCES game(game_id) ON DELETE CASCADE ON UPDATE CASCADE,
                CONSTRAINT FK_player_id_itemsInGame_players FOREIGN KEY (player_id)
                REFERENCES players(mc_uuid)
            )
            """;

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
            Connection dbConn = DatabaseManager.getConnection();

            // Allow for multi query if is success

            String query = "'SET GLOBAL (DSQEC_RUN_MQ=1)";

            PreparedStatement stmt = dbConn.prepareStatement(query);

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

        try {
            Connection dbConn = DatabaseManager.getConnection();

            Statement stmt = dbConn.createStatement();

            stmt.addBatch(DB_CREATION_SCRIPT_GAME);
            stmt.addBatch(DB_CREATION_SCRIPT_TEAMS);
            stmt.addBatch(DB_CREATION_SCRIPT_PLAYERS);
            stmt.addBatch(DB_CREATION_SCRIPT_TEAMINGAME);
            stmt.addBatch(DB_CREATION_SCRIPT_CATEGORIES);
            stmt.addBatch(DB_CREATION_SCRIPT_ITEMS);
            stmt.addBatch(DB_CREATION_SCRIPT_ITEMHISTORYINCATEGORY);
            stmt.addBatch(DB_CREATION_SCRIPT_ITEMSINGAME);

            stmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
