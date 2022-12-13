package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A class designed to have common methods to format queries, disable/enable key checks, and initialize the table.
 * Common conventions for subclasses is to declare the SQL queries as String literals at the top of the file,
 * then write the methods to update parameters below. These classes only handle the execution of queries for a
 * specific database, in a specific table.
 */
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

    /**
     * Internal method used to disable key checks for foreign key constraints.
     * Used before updating a field that has a reference to a foreign key to avoid errors.
     * @param connectionHandler the connection handler to execute this command on
     */
    protected void disableKeyChecks(ConnectionHandler connectionHandler) {
        setKeyChecks(connectionHandler, KEY_CHECKS_DISABLED);
    }

    /**
     * Internal method used to re-enable key checks for foreign key constraints.
     * Used after updating a field that has a reference to a foreign key to avoid errors.
     * @param connectionHandler the connection handler to execute this command on
     */
    protected void enableKeyChecks(ConnectionHandler connectionHandler) {
        setKeyChecks(connectionHandler, KEY_CHECKS_ENABLED);
    }

    /**
     * Sets the key checks variable to be the value of the given parameter
     * @param connectionHandler the connection handler to execute this command on
     * @param parameter the integer boolean parameter to disable/enable key checks
     */
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