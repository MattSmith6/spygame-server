package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AuthenticationTable;
import com.github.spygameserver.database.table.VerificationTokenTable;

// The authentication database only stores authentication data necessary for TLS-SRP
public class AuthenticationDatabase extends AbstractDatabase {

    private final AuthenticationTable authenticationTable;
    private final VerificationTokenTable verificationTokenTable;

    public AuthenticationDatabase(DatabaseConnectionManager databaseConnectionManager) {
        super(databaseConnectionManager);

        this.authenticationTable = new AuthenticationTable();
        this.verificationTokenTable = new VerificationTokenTable();

        initialize(authenticationTable, verificationTokenTable);
    }

    public AuthenticationTable getAuthenticationTable() {
        return authenticationTable;
    }

    public VerificationTokenTable getVerificationTokenTable() {
        return verificationTokenTable;
    }

}
