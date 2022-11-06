package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AbstractTable;

import java.sql.Connection;

public abstract class AbstractDatabase {

    protected final DatabaseConnectionManager databaseConnectionManager;
    protected final boolean useTestTables;

    private boolean isInitialized = false;

    protected AbstractDatabase(DatabaseConnectionManager databaseConnectionManager, boolean useTestTables) {
        this.databaseConnectionManager = databaseConnectionManager;
        this.useTestTables = useTestTables;
    }

    // Initialize all tables by creating them, if necessary
    protected void initialize(AbstractTable... tables) {
        ConnectionHandler connectionHandler = getNewConnectionHandler(false);

        for (AbstractTable table : tables) {
            table.initialize(connectionHandler);
        }

        connectionHandler.setShouldCloseConnectionAfterUse(true);
        connectionHandler.closeConnectionIfNecessary();

        this.isInitialized = true;
    }

    protected void throwExceptionIfUninitialized() {
        if (!isInitialized) {
            throw new RuntimeException("Database is not initialized. Call the initialize method in the constructor " +
                    "of your database.");
        }
    }

    public ConnectionHandler getNewConnectionHandler(boolean shouldCloseConnectionAfterUsed) {
        throwExceptionIfUninitialized();
        Connection connection = databaseConnectionManager.createNewConnection();

        return new ConnectionHandler(connection, shouldCloseConnectionAfterUsed);
    }

}
