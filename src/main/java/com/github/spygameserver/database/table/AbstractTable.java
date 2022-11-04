package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

public abstract class AbstractTable {

    private static final String TESTING_TABLE_PREFIX = "test_";

    private final String tableName;

    protected AbstractTable(String nonTestingTableName, boolean useTestTables) {
        this.tableName = useTestTables ? TESTING_TABLE_PREFIX + nonTestingTableName : nonTestingTableName;
    }

    public abstract boolean createTableIfNotExists(ConnectionHandler connectionHandler);

    protected String getTableName() {
        return this.tableName;
    }

    protected String formatQuery(String queryToFormat) {
        return String.format(queryToFormat, getTableName());
    }

}
