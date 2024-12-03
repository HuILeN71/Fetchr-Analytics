package nl.dacolina.fetchranalytics;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FetchrAnalytics implements ModInitializer {
	public static final String MOD_ID = "fetchranalytics";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String DB_URL = "jdbc:mysql://10.0.10.10:3306/lucky-db";
	private static final String USER = "lucky";
	private static final String PASSWORD = "";

	@Override
	public void onInitialize() {
		try {
			// Connect to the database

			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				// Or "com.mysql.jdbc.Driver" for some old mysql driver
			} catch(ClassNotFoundException e) {
				System.out.println("Failed to load driver!");
				e.printStackTrace();
			}

			Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			System.out.println("Database connection successful!");

			// Perform database operations here...

			connection.close();
		} catch (SQLException e) {
			System.err.println("MySQL JDBC Driver not found!");
			e.printStackTrace();
		}
	}
}