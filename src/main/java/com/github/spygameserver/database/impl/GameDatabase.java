package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.AuthenticationTable;
import com.github.spygameserver.database.table.GameLobbyTable;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.database.table.PlayerGameInfoTable;

// The game database includes all tables unrelated to authentication:
// The player's account, player game info, game lobbies, game records, etc.
public class GameDatabase extends AbstractDatabase {

    private final PlayerAccountTable playerAccountTable;
    private final GameLobbyTable gameLobbyTable;
    private final PlayerGameInfoTable playerGameInfoTable;

    public GameDatabase(DatabaseConnectionManager databaseConnectionManager) {
        super(databaseConnectionManager);

        this.playerAccountTable = new PlayerAccountTable();
        this.gameLobbyTable = new GameLobbyTable();
        this.playerGameInfoTable = new PlayerGameInfoTable();

        initialize(playerAccountTable, gameLobbyTable, playerGameInfoTable);
    }

    public PlayerAccountTable getPlayerAccountTable() {
        return playerAccountTable;
    }

    public GameLobbyTable getGameLobbyTable() {
        return this.gameLobbyTable;
    }

    public PlayerGameInfoTable getPlayerGameInfoTable() {
        return this.playerGameInfoTable;
    }

}
