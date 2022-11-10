package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AuthenticationTable;

// The authentication database only stores authentication data necessary for TLS-SRP
public class AuthenticationDatabase extends AbstractDatabase {

    private final AuthenticationTable authenticationTable;

    public AuthenticationDatabase(DatabaseConnectionManager databaseConnectionManager, boolean useTestTables) {
        super(databaseConnectionManager, useTestTables);

        this.authenticationTable = new AuthenticationTable(useTestTables);
        initialize(authenticationTable);
    }

    public AuthenticationTable getAuthenticationTable() {
        return authenticationTable;
    }

}
