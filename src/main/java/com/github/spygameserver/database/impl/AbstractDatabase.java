package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AbstractTable;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDatabase implements AutoCloseable {

    protected final DatabaseConnectionManager databaseConnectionManager;
    protected final boolean useTestTables;

    private boolean isInitialized = false;

    protected AbstractDatabase(DatabaseConnectionManager databaseConnectionManager, boolean useTestTables) {
        this.databaseConnectionManager = databaseConnectionManager;
        this.useTestTables = useTestTables;
    }

    // Initialize all tables by creating them, if necessary
    protected void initialize(AbstractTable... tables) {
        ConnectionHandler connectionHandler = getUncheckedConnectionHandler(false);

        for (AbstractTable table : tables) {
            table.initialize(connectionHandler);
        }

        connectionHandler.closeAbsolutely();

        this.isInitialized = true;
    }

    protected void throwExceptionIfUninitialized() {
        if (!isInitialized) {
            throw new IllegalStateException("Database is not initialized. Call the initialize method in the constructor "
                    + "of your database.");
        }
    }

    private ConnectionHandler getUncheckedConnectionHandler(boolean shouldCloseConnectionAfterUsed) {
        try {
            Connection connection = databaseConnectionManager.createNewConnection();
            return new ConnectionHandler(connection, shouldCloseConnectionAfterUsed);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public ConnectionHandler getNewConnectionHandler(boolean shouldCloseConnectionAfterUsed) {
        throwExceptionIfUninitialized();

        // Now that we've thrown an exception, this is no longer unchecked
        return getUncheckedConnectionHandler(shouldCloseConnectionAfterUsed);
    }

    @Override
    public void close() {
        databaseConnectionManager.close();
    }

}
