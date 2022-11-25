package com.github.spygameserver.database.table;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;
import com.github.spygameserver.database.table.GameLobbyTable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameLobbyTableTest implements DatabaseRequiredTest {

    private GameDatabase gameDatabase;
    private GameLobbyTable gameLobbyTable;
    private ConnectionHandler connectionHandler;

    @BeforeAll
    public void setupConnection() {
        gameDatabase = getGameDatabase();

        gameLobbyTable = gameDatabase.getGameLobbyTable();
        connectionHandler = gameDatabase.getNewConnectionHandler(false);

        // Make sure no data persists, since these fields need to be unique
        gameLobbyTable.dropTableSecure(connectionHandler);
        gameLobbyTable.initialize(connectionHandler);
    }

    @Test
    public void testAllPaths() {
        String inviteCode = null;
        String gameName = "Test Game";

        gameLobbyTable.createTableIfNotExists(connectionHandler);
        gameLobbyTable.createGame(connectionHandler, 0, 0, 1, gameName);
        inviteCode = gameLobbyTable.getInviteCode(connectionHandler, 1);
        GameLobbyTable.Pair<Integer, Long> gameStuff = gameLobbyTable.getGameIdFromInviteCode(connectionHandler, inviteCode);

        Assertions.assertTrue(gameStuff.getR() == null);
        Assertions.assertTrue(gameLobbyTable.getCurrentPlayers(connectionHandler, gameStuff.getL()) == 0);

        gameLobbyTable.updateCurrentPlayers(connectionHandler, 5, gameStuff.getL());

        Assertions.assertTrue(gameLobbyTable.getCurrentPlayers(connectionHandler, gameStuff.getL()) == 5);

        GameLobbyTable.game testGame = new GameLobbyTable.game();
        testGame = gameLobbyTable.showAll(connectionHandler, inviteCode);

        Assertions.assertTrue(testGame.startTime == null);
        Assertions.assertTrue(testGame.endTime == null);

        gameLobbyTable.updateStartTime(connectionHandler);
        gameLobbyTable.updateEndTime(connectionHandler);

        Assertions.assertTrue(testGame.startTime != null);
        Assertions.assertTrue(testGame.endTime != null);
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(connectionHandler);
        closeOpenConnections(gameDatabase);
    }

}
