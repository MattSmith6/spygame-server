package com.github.spygameserver.database.table;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.impl.GameDatabase;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerGameInfoTableTest implements DatabaseRequiredTest {

    private GameDatabase gameDatabase;
    private PlayerGameInfoTable playerGameInfoTable;
    private ConnectionHandler connectionHandler;

    @BeforeAll
    public void setupConnection() {
        gameDatabase = getGameDatabase();

        playerGameInfoTable = gameDatabase.getPlayerGameInfoTable();
        connectionHandler = gameDatabase.getNewConnectionHandler(false);

        gameDatabase.getGameLobbyTable().createTableIfNotExists(connectionHandler);
        gameDatabase.getPlayerAccountTable().createTableIfNotExists(connectionHandler);

        playerGameInfoTable.initialize(connectionHandler);
    }

    @Test
    public void testAllPaths() {
        int playerID = 1;
        int playerIDTest = 0;
        int currentGame = 2;
        int currentGameTest = 0;

        playerGameInfoTable.createPlayer(connectionHandler, playerID);
        playerIDTest = playerGameInfoTable.getCurrentNumber(connectionHandler, "player_id", playerID);

        Assertions.assertEquals(playerIDTest, playerID);

        playerGameInfoTable.updateCurrentGameId(connectionHandler, currentGame, playerID);
        currentGameTest = playerGameInfoTable.getCurrentNumber(connectionHandler, "current_game_id", playerID);

        Assertions.assertEquals(currentGame, currentGameTest);
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(connectionHandler);
        closeOpenConnections(gameDatabase);
    }

}
