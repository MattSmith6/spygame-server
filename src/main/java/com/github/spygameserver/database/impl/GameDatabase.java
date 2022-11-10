package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AuthenticationTable;

// The game database includes all tables unrelated to authentication:
// The player's account, player game info, game lobbies, game records, etc.
public class GameDatabase extends AbstractDatabase {

    public GameDatabase(DatabaseConnectionManager databaseConnectionManager, boolean useTestTables) {
        super(databaseConnectionManager, useTestTables);
    }

}
