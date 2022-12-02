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

        gameLobbyTable.initialize(connectionHandler);
    }

    @Test
    public void testAllPaths() {
        String inviteCode = null;
        String gameName = "Test Game";
        int gameID;

        gameID = gameLobbyTable.createGame(connectionHandler, 0, 0, 1, gameName);
        inviteCode = gameLobbyTable.getInviteCode(connectionHandler, gameID);
        GameLobbyTable.Pair<Integer, Long> gameStuff = gameLobbyTable.getGameIdFromInviteCode(connectionHandler, inviteCode);

        Assertions.assertTrue(gameStuff.getR() == null);
        Assertions.assertEquals(gameLobbyTable.getCurrentPlayers(connectionHandler, gameStuff.getL()), 0);

        gameLobbyTable.updateCurrentPlayers(connectionHandler, 5, gameStuff.getL());

        Assertions.assertTrue(gameLobbyTable.getCurrentPlayers(connectionHandler, gameStuff.getL()) == 5);

        GameLobbyTable.game testGame = new GameLobbyTable.game();
        testGame = gameLobbyTable.showAll(connectionHandler, inviteCode);

        Assertions.assertNull(testGame.startTime);
        Assertions.assertNull(testGame.endTime);

        gameLobbyTable.updateStartTime(connectionHandler, gameID);
        gameLobbyTable.updateEndTime(connectionHandler, gameID);

        testGame = gameLobbyTable.showAll(connectionHandler, inviteCode);

        Assertions.assertNotNull(testGame.startTime);
        Assertions.assertNotNull(testGame.endTime);
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(connectionHandler);
        closeOpenConnections(gameDatabase);
    }

}
