package com.github.spygameserver.database.impl;

import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.table.GameLobbyTable;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.database.table.PlayerGameInfoTable;

/**
 * A class representing the SQL game database. Includes references to the player account table, game lobby table,
 * player game info table, and the game records table if implemented by project submission.
 */
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
