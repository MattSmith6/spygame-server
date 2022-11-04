package com.github.spygameserver.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
