package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AuthenticationTable;

public class AuthenticationDatabase extends AbstractDatabase {

    private final AuthenticationTable authenticationTable;

    public AuthenticationDatabase(DatabaseConnectionManager databaseConnectionManager, boolean useTestTables) {
        super(databaseConnectionManager, useTestTables);

        this.authenticationTable = new AuthenticationTable(useTestTables);
        this.hasCreatedAnyTables = initialize(authenticationTable);
    }

    public AuthenticationTable getAuthenticationTable() {
        return authenticationTable;
    }

}
