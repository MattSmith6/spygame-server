package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

public abstract class AbstractTable {

    private static final String TESTING_TABLE_PREFIX = "test_";

    private final String tableName;
    private boolean wasTableCreated;

    protected AbstractTable(String nonTestingTableName, boolean useTestTables) {
        this.tableName = useTestTables ? TESTING_TABLE_PREFIX + nonTestingTableName : nonTestingTableName;
    }
    public void initialize(ConnectionHandler connectionHandler) {
        this.wasTableCreated = createTableIfNotExists(connectionHandler);
    }

    public boolean wasTableCreated() {
        return this.wasTableCreated;
    }

    /**
     * Creates the designated table in the SQL database from the provided ConnectionHandler.
     * @param connectionHandler provides the connection to update the database
     * @return true if the table was created, false if the table was not created
     */
    protected abstract boolean createTableIfNotExists(ConnectionHandler connectionHandler);

    protected String getTableName() {
        return this.tableName;
    }

    protected String formatQuery(String queryToFormat) {
        return String.format(queryToFormat, getTableName());
    }

}
