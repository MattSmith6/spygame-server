package com.github.spygameserver.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// For now, this class is designed as a way to open up connections. If there is time, this may include a way
// to open a connection pool, where connections are shared between a certain number of threads
public class DatabaseConnectionManager {

    private final String databaseConnectionUrl;
    private final String username;
    private final String password;

    public DatabaseConnectionManager(DatabaseCredentialsProcessor databaseCredentialsProcessor) {
        this.databaseConnectionUrl = databaseCredentialsProcessor.getDatabaseConnectionUrl();
        this.username = databaseCredentialsProcessor.getUsername();
        this.password = databaseCredentialsProcessor.getPassword();
    }

    public Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(databaseConnectionUrl, username, password);
    }

}
