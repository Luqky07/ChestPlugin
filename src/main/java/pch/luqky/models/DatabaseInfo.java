package pch.luqky.models;

//Class to manage database info
public class DatabaseInfo {
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
    
    public DatabaseInfo(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }
    
    public String getConnectionString() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
