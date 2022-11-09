package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractTable {

    private static final String CHECK_IF_EMPTY_QUERY = "SELECT 1 FROM %s LIMIT 1";

    private static final String TESTING_TABLE_PREFIX = "test_";

    private final String tableName;

    protected AbstractTable(String nonTestingTableName, boolean useTestTables) {
        this.tableName = useTestTables ? TESTING_TABLE_PREFIX + nonTestingTableName : nonTestingTableName;
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

    public boolean isTableEmpty(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String checkIfEmptyQuery = formatQuery(CHECK_IF_EMPTY_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(checkIfEmptyQuery)) {
            boolean hasOneRecord = preparedStatement.executeQuery().next();

            // If the table does not have one record, then the table is empty
            return !hasOneRecord;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

}
