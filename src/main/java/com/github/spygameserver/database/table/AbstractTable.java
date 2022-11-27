package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractTable {

    private static final String SET_FOREIGN_KEY_CHECK_QUERY = "SET foreign_key_checks=?";

    private static final int KEY_CHECKS_ENABLED = 1;
    private static final int KEY_CHECKS_DISABLED = 0;

    private final String tableName;

    protected AbstractTable(TableType tableType) {
        this.tableName = tableType.getTableName();
    }

    public void initialize(ConnectionHandler connectionHandler) {
        createTableIfNotExists(connectionHandler);
    }

    /**
     * Creates the designated table in the SQL database from the provided ConnectionHandler.
     * @param connectionHandler provides the connection to update the database
     */
    protected abstract void createTableIfNotExists(ConnectionHandler connectionHandler);

    protected String getTableName() {
        return this.tableName;
    }

    protected String formatQuery(String queryToFormat) {
        return String.format(queryToFormat, getTableName());
    }

    protected void disableKeyChecks(ConnectionHandler connectionHandler) {
        setKeyChecks(connectionHandler, KEY_CHECKS_DISABLED);
    }

    protected void enableKeyChecks(ConnectionHandler connectionHandler) {
        setKeyChecks(connectionHandler, KEY_CHECKS_ENABLED);
    }

    protected void setKeyChecks(ConnectionHandler connectionHandler, int parameter) {
        Connection connection = connectionHandler.getConnection();
        String setKeyChecksQuery = formatQuery(SET_FOREIGN_KEY_CHECK_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(setKeyChecksQuery)) {
            preparedStatement.setInt(1, parameter);

            preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}