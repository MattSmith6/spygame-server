package com.github.spygameserver.database;

import com.github.spygameserver.util.ExceptionHandling;

import java.sql.Connection;

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
