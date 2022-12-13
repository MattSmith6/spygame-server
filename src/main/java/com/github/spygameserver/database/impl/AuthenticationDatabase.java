package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.PlayerAuthenticationTable;
import com.github.spygameserver.database.table.VerificationTokenTable;

/**
 * The class representing the SQL authentication database. Includes references to the authentication tables
 * and verification token tables.
 */
public class AuthenticationDatabase extends AbstractDatabase {

    private final PlayerAuthenticationTable authenticationTable;
    private final VerificationTokenTable verificationTokenTable;

    public AuthenticationDatabase(DatabaseConnectionManager databaseConnectionManager) {
        super(databaseConnectionManager);

        this.authenticationTable = new PlayerAuthenticationTable();
        this.verificationTokenTable = new VerificationTokenTable();

        initialize(authenticationTable, verificationTokenTable);
    }

    public PlayerAuthenticationTable getAuthenticationTable() {
        return authenticationTable;
    }

    public VerificationTokenTable getVerificationTokenTable() {
        return verificationTokenTable;
    }

}
