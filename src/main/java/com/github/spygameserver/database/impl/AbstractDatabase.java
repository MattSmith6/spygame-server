package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AbstractTable;

import java.sql.Connection;

public class AbstractDatabase {

    protected final DatabaseConnectionManager databaseConnectionManager;
    protected final boolean useTestTables;

    protected boolean hasCreatedAnyTables;

    protected AbstractDatabase(DatabaseConnectionManager databaseConnectionManager, boolean useTestTables) {
        this.databaseConnectionManager = databaseConnectionManager;
        this.useTestTables = useTestTables;
    }

    // Return true if any of the tables needed to be created
    public boolean initialize(AbstractTable... tables) {
        ConnectionHandler connectionHandler = getNewConnectionHandler(false);

        boolean hasAnyTableBeenCreated = false;

        for (AbstractTable table : tables) {
            if (table.createTableIfNotExists(connectionHandler)) {
                hasAnyTableBeenCreated = true;
                break;
            }
        }

        connectionHandler.setShouldCloseConnectionAfterUse(true);
        connectionHandler.closeConnectionIfNecessary();

        return hasAnyTableBeenCreated;
    }

    public boolean hasCreatedAnyTables() {
        return hasCreatedAnyTables;
    }

    public ConnectionHandler getNewConnectionHandler(boolean shouldCloseConnectionAfterUsed) {
        Connection connection = databaseConnectionManager.createNewConnection();

        return new ConnectionHandler(connection, shouldCloseConnectionAfterUsed);
    }

}
