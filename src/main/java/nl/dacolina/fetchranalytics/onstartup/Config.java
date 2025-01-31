package nl.dacolina.fetchranalytics.onstartup;

import net.fabricmc.loader.api.FabricLoader;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.managers.DatabaseManager;
import nl.dacolina.fetchranalytics.managers.DatapackManager;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class Config {

    private static final String[] DATABASE_TABLES = {"servers", "game", "gamemodes", "teams", "players", "teamInGame", "playerInGame", "categories", "items",
            "itemHistoryInCategory", "bingoCardItems", "itemsInGame"};
    private static final String[] FETCHR_GAMEMODES = {"none", "Line", "Black_out", "Lockout", "20_Items_no_bingo", "Blind_mode", "Max_points_in_25_minutes"};
    private static final String CONFIG_FILE_NAME = "fetchranalytics-settings.toml";
    private static File configFile;

    private String connectString;
    private String userName;
    private String password;
    private String databaseName;
    public String error = "";

    public Config() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path rootDir = FabricLoader.getInstance().getGameDir();

        configFile = new File(String.valueOf(configDir), CONFIG_FILE_NAME);

        DatapackManager datapackManager = new DatapackManager(rootDir);

        if(!configFile.exists()) {

            FetchrAnalytics.LOGGER.info("Settings file does not yet exist, creating one!");

            createConfig(String.valueOf(configDir));
        }

        FetchrAnalytics.LOGGER.info("Found settings file, loading settings!");
        loadConfig(String.valueOf(configDir));

    }

    private static void createConfig(String configDirectory) {
        String configTomlTemplate = """
                title = "Fetchr Analytics Settings"
                
                [database_settings]
                
                # This is the database name e.g fetchrdb
                name = ""
                
                # This is the database IP or DNS name e.g 10.0.0.1 or db.example.com
                ip = ""
                
                # This is the database IP or DNS name e.g. 3306
                port = ""
                
                # This is the database user, ensure it has access to the database! e.g. fetchrUser
                username = ""
                
                # The users password e.g. VerySecureP@ssw0rd!
                password = ""
                
                # Temporary server ID
                servername = 1
                """;
        try {
            File file = new File(configDirectory + File.separator + CONFIG_FILE_NAME);

            // Create mew file
            if(file.createNewFile()) {
                FetchrAnalytics.LOGGER.info("Successfully created new config file!");

                try (FileWriter writer = new FileWriter(file)){
                    writer.write(configTomlTemplate);
                    FetchrAnalytics.LOGGER.info("Successfully written default template in config file!");
                }
            }

        } catch (IOException e) {
            System.out.println("Some error has occurred :(");
            e.printStackTrace();
        }


    }

    private void loadConfig(String configDirectory) {
        // Store file location in a variable
        Path settingFile = Path.of( configDirectory + File.separator + CONFIG_FILE_NAME);

        try {
            TomlParseResult result = Toml.parse(settingFile);

            // Check if the file has errors
            if(result.hasErrors()) {
                result.errors().forEach(error -> System.err.println("Error: " + error.toString()));
                this.error = "File has parsing errors! Check the config file!";
            }

            if (this.error.isEmpty()) {

                var databaseSettings = result.getTable("database_settings");

                // Check if the database settings table is available in TOML file
                if(databaseSettings != null) {
                    String ip = databaseSettings.getString("ip");

                    // Each clause represents a check to detect whether key is missing or value is empty
                    if(ip == null || ip.isEmpty()) {
                        this.error += "Missing IP address in config file!\n";
                    }

                    String port = databaseSettings.getString("port");

                    if(port == null || port.isEmpty()) {
                        this.error += "Missing port in config file!";
                    }

                    this.databaseName = databaseSettings.getString("name");

                    if(this.databaseName == null || this.databaseName.isEmpty()) {
                        this.error += "Missing database name in config file!";
                    }

                    this.userName = databaseSettings.getString("username");

                    if(this.userName == null || this.userName.isEmpty()) {
                        this.error += "Missing username in config file!";
                    }

                    this.password = databaseSettings.getString("password");

                    if(this.password == null || this.password.isEmpty()) {
                        this.error += "Missing password in config file!";
                    }

                    if (this.error.isEmpty()) {
                        // If there are no errors Create the MYSQL Connector string.
                        this.connectString = "jdbc:mariadb://" + ip + ":" + port + "/" + databaseName;
                    }


                } else {
                    FetchrAnalytics.LOGGER.error("Missing database settings section in config file!");
                }

            }

        } catch (Exception e) {
            System.out.println(e);

        }

    }

    public String getConnectString() {
        return this.connectString;
    }

    public String getDatabaseUser() {
        return this.userName;
    }

    public String getUserPassword() {
        return this.password;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public static String[] getDatabaseTables() {
        return DATABASE_TABLES;
    }

    public static String[] getFetchrGamemodes() {
        return FETCHR_GAMEMODES;
    }

    public static int getServerID () {

        int serverID = -1;

        FabricLoader.getInstance().getConfigDir();

        Path settingFile = Path.of(FabricLoader.getInstance().getConfigDir() + File.separator + CONFIG_FILE_NAME);

        try {
            TomlParseResult result = Toml.parse(settingFile);

            var databaseSettings = result.getTable("database_settings");

            if(databaseSettings != null) {
                serverID = Math.toIntExact(databaseSettings.getLong("servername"));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverID;
    }

}
