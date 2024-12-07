package nl.dacolina.fetchranalytics.onstartup;

import java.sql.DriverManager;
import java.sql.SQLException;

public class CheckDatabase {

    public static boolean areAllTablesAvailable() {
        return false;
    }

    public static boolean ableToConnect(String connectUrl, String userName, String password) {

        try {
            DriverManager.getConnection(connectUrl, userName, password);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
