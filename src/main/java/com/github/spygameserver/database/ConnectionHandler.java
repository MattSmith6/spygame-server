package com.github.spygameserver.database;

import com.github.spygameserver.util.ExceptionHandling;

import java.sql.Connection;

/**
 * A class used to hold a Connection object to connect to the database, as well as a boolean for whether
 * to close the connection after use. This allows for Connection objects to be left open and checked for closing
 * after each use, if necessary, while also serving as a reminder to close these volatile connections.
 */
public class ConnectionHandler {

    private final Connection connection;
    private boolean shouldCloseConnectionAfterUse;

    public ConnectionHandler(Connection connection, boolean shouldCloseConnectionAfterUse) {
        this.connection = connection;
        this.shouldCloseConnectionAfterUse = shouldCloseConnectionAfterUse;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnectionIfNecessary() {
        if (shouldCloseConnectionAfterUse) {
            ExceptionHandling.closeQuietly(getConnection());
        }
    }

    public void setShouldCloseConnectionAfterUse(boolean shouldCloseConnectionAfterUse) {
        this.shouldCloseConnectionAfterUse = shouldCloseConnectionAfterUse;
    }

    public void closeAbsolutely() {
        ExceptionHandling.closeQuietly(getConnection());
    }

}
