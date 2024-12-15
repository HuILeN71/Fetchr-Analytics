package nl.dacolina.fetchranalytics.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static HikariDataSource dataSource;

    private static final int MAX_POOL_SIZE = 10;
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int CONNECTION_MAX_LIFETIME = 600000;
    private static final int CONNECTION_IDLE_TIMEOUT = 1800000;

    public static void createConnectionPool(String connectionURL, String userName, String password) {

        HikariConfig databaseConfig = new HikariConfig();

        // Setup settings
        databaseConfig.setJdbcUrl(connectionURL);
        databaseConfig.setUsername(userName);
        databaseConfig.setPassword(password);

        // Other pool settings
        databaseConfig.setMaximumPoolSize(MAX_POOL_SIZE);
        databaseConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        databaseConfig.setIdleTimeout(CONNECTION_IDLE_TIMEOUT);
        databaseConfig.setMaxLifetime(CONNECTION_MAX_LIFETIME);

        dataSource = new HikariDataSource(databaseConfig);

    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

}
