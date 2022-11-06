package com.github.spygameserver.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// For now, this class is designed as a way to open up connections. If there is time, this may include a way
// to open a connection pool, where connections are shared between a certain number of threads
public class DatabaseConnectionManager {

    private final DatabaseCredentialsProcessor databaseCredentialsProcessor;

    public DatabaseConnectionManager(DatabaseCredentialsProcessor databaseCredentialsProcessor) {
        this.databaseCredentialsProcessor = databaseCredentialsProcessor;
    }

    public Connection createNewConnection() {
        String databaseConnectionUrl = databaseCredentialsProcessor.getDatabaseConnectionUrl();
        String username = databaseCredentialsProcessor.getUsername();
        String password = databaseCredentialsProcessor.getPassword();

        try {
            return DriverManager.getConnection(databaseConnectionUrl, username, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
