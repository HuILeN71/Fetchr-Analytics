package nl.dacolina.fetchranalytics;

public class FetchrAnalyticsSQL {
    public String db_url;
    public String db_port;
    public String db_user;
    public String db_password;
    public String db;
    public String connectionString;

    public FetchrAnalyticsSQL(String db_url, String db_port, String db_user, String db_password, String db) {
        this.db = db;
        this.db_url = db_url;
        this.db_port = db_port;
        this.db_user = db_port;
        this.db_password = db_password;
        this.connectionString = createConnectString(this.db_url, this.db_port, this.db);

    }

    private String createConnectString(String db_url, String db_port, String db) {
        return "jdbc:mysql://" + db_url + ':' + db_port + '/' + db;
    }

}