package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractTable {

    private static final String CHECK_IF_EMPTY_QUERY = "SELECT 1 FROM %s LIMIT 1";
    private static final String DROP_TABLE_QUERY = "DROP TABLE %s";

    private static final String TESTING_TABLE_PREFIX = "test_";

    private final String tableName;
    private final boolean useTestTables;

    protected AbstractTable(String nonTestingTableName, boolean useTestTables) {
        this.tableName = useTestTables ? TESTING_TABLE_PREFIX + nonTestingTableName : nonTestingTableName;
        this.useTestTables = useTestTables;
    }

    public void initialize(ConnectionHandler connectionHandler) {
        createTableIfNotExists(connectionHandler);
    }

    /**
     * Creates the designated table in the SQL database from the provided ConnectionHandler.
     * @param connectionHandler provides the connection to update the database
     */
    protected abstract void createTableIfNotExists(ConnectionHandler connectionHandler);

    // Drop the table only if test table is being used, will not drop a production table
    public void dropTableSecure(ConnectionHandler connectionHandler) {
        if (!useTestTables) {
            throw new IllegalStateException("Cannot securely drop a table that is not a test table.");
        }

        Connection connection = connectionHandler.getConnection();
        String dropTableQuery = formatQuery(DROP_TABLE_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(dropTableQuery)) {
            preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected String getTableName() {
        return this.tableName;
    }

    protected String formatQuery(String queryToFormat) {
        return String.format(queryToFormat, getTableName());
    }

    public boolean isTableEmpty(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String checkIfEmptyQuery = formatQuery(CHECK_IF_EMPTY_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(checkIfEmptyQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is no record to go to next, then the table is empty
                return !resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

}
