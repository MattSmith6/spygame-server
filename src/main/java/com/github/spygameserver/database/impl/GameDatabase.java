package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.PlayerAccountTable;

// The game database includes all tables unrelated to authentication:
// The player's account, player game info, game lobbies, game records, etc.
public class GameDatabase extends AbstractDatabase {

    private final PlayerAccountTable playerAccountTable;

    public GameDatabase(DatabaseConnectionManager databaseConnectionManager, boolean useTestTables) {
        super(databaseConnectionManager, useTestTables);

        this.playerAccountTable = new PlayerAccountTable(useTestTables);

        initialize(playerAccountTable);
    }

    public PlayerAccountTable getPlayerAccountTable() {
        return playerAccountTable;
    }

}
