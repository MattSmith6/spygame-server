package com.github.spygameserver.auth;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.caesar.Hex;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.AuthenticationTable;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.ByteOrder;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerAuthenticationHandshakeTest implements DatabaseRequiredTest {

    private static final String TEST_EMAIL = "alice_fake@my.csun.edu";
    private static final String TEST_USERNAME = "alice";

    private static final Bytes TEST_SALT = new Hex("BEB25379 D1A8581E B5A72767 3A2441EE");
    private static final SRP6CustomIntegerVariable TEST_VERIFIER = new SRP6CustomIntegerVariable(
            new Hex("7E273DE8 696FFC4F 4E337D05 B4B375BE B0DDE156 9E8FA00A 9886D812" +
                    "         9BADA1F1 822223CA 1A605B53 0E379BA4 729FDC59 F105B478 7E5186F5" +
                    "         C671085A 1447B52A 48CF1970 B4FB6F84 00BBF4CE BFBB1681 52E08AB5" +
                    "         EA53D15C 1AFF87B2 B9DA6E04 E058AD51 CC72BFC9 033B564E 26480D78" +
                    "         E955A5E2 9E7AB245 DB2BE315 E2099AFB"), ByteOrder.BIG_ENDIAN);

    public static final Logger LOG = Logger.getLogger(ServerAuthenticationHandshakeTest.class.getName());

    private GameDatabase gameDatabase;
    private AuthenticationDatabase authenticationDatabase;

    @BeforeAll
    public void setupDatabaseConnection() {
        File databaseCredentialsFile = getValidCredentialsFile();

        DatabaseCreator<AuthenticationDatabase> authenticationDatabaseCreator = new DatabaseCreator<>(
                databaseCredentialsFile, "auth_db", true);
        authenticationDatabase = authenticationDatabaseCreator.createDatabaseFromFile(AuthenticationDatabase::new);

        authenticationDatabase.getAuthenticationTable().dropTableSecure(authenticationDatabase.getNewConnectionHandler(true));
        authenticationDatabase.getAuthenticationTable().createTableIfNotExists(authenticationDatabase.getNewConnectionHandler(true));

        DatabaseCreator<GameDatabase> gameDatabaseDatabaseCreator = new DatabaseCreator<>(databaseCredentialsFile,
                "game_db", true);
        gameDatabase = gameDatabaseDatabaseCreator.createDatabaseFromFile(GameDatabase::new);

        insertTestDataIfNecessary();
    }

    public void insertTestDataIfNecessary() {
        PlayerAccountTable playerAccountTable = gameDatabase.getPlayerAccountTable();
        ConnectionHandler gameConnectionHandler = gameDatabase.getNewConnectionHandler(false);

        PlayerAccountData playerAccountData = playerAccountTable.getPlayerAccountDataByEmail(gameConnectionHandler, TEST_EMAIL);
        if (playerAccountData != null) {
            gameConnectionHandler.closeAbsolutely();
            return;
        }

        playerAccountTable.addVerifiedEmail(gameConnectionHandler, TEST_EMAIL);
        playerAccountTable.addUsernameToPlayerAccount(gameConnectionHandler, TEST_EMAIL, TEST_USERNAME);

        int playerId = playerAccountTable.getPlayerIdByUsername(gameConnectionHandler, TEST_USERNAME);

        AuthenticationTable authenticationTable = authenticationDatabase.getAuthenticationTable();
        ConnectionHandler authConnectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        // If the table our data already exists, we don't need to be here
        if (authenticationTable.getPlayerAuthenticationRecord(authConnectionHandler, playerId) != null) {
            authConnectionHandler.closeAbsolutely();
            return;
        }

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId,
                TEST_SALT, TEST_VERIFIER);

        authenticationTable.addPlayerAuthenticationRecord(authConnectionHandler, playerAuthenticationData);
        authConnectionHandler.closeAbsolutely();
    }

    @Test
    public void testValidPlayerReceiveHello() {
        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        int playerId = gameDatabase.getPlayerAccountTable().getPlayerIdByUsername(connectionHandler, TEST_USERNAME);

        ServerAuthenticationHandshake handshake = new ServerAuthenticationHandshake(TEST_USERNAME, authenticationDatabase);
        JSONObject responseToPlayerHello = handshake.respondToHello(playerId);

        if (responseToPlayerHello.has("error")) {
            Assertions.fail(responseToPlayerHello.getString("error"));
        }

        SRP6IntegerVariable A = new SRP6CustomIntegerVariable(
                new Hex("61D5E490 F6F1B795 47B0704C 436F523D D0E560F0 C64115BB 72557EC4" +
                "         4352E890 3211C046 92272D8B 2D1A5358 A2CF1B6E 0BFCF99F 921530EC" +
                "         8E393561 79EAE45E 42BA92AE ACED8251 71E1E8B9 AF6D9C03 E1327F44" +
                "         BE087EF0 6530E69F 66615261 EEF54073 CA11CF58 58F0EDFD FE15EFEA" +
                "         B349EF5D 76988A36 72FAC47B 0769447B"), ByteOrder.BIG_ENDIAN);

        Bytes M1 = new Hex("B0DC82BA BCF30674 AE450C02 87745E79 90A3381F 63B387AA F271A10D" +
                        "         233861E3 59B48220 F7C4693C 9AE12B0A 6F67809F 0876E2D0 13800D6C" +
                        "         41BB59B6 D5979B5C 00A172B4 A2A5903A 0BDCAF8A 709585EB 2AFAFA8F" +
                        "         3499B200 210DCC1F 10EB3394 3CD67FC8 8A2F39A4 BE5BEC4E C0A3212D" +
                        "         C346D7E4 74B29EDE 8A469FFE CA686E5A");

        JSONObject responseToKeyExchange = handshake.respondToKeyExchange(A, M1);

        if (responseToKeyExchange.has("error")) {
            return;
        }

        Bytes M2 = Bytes.wrapped(handshake.getPremasterSecret());
        Assertions.assertEquals(M1, M2, "M2 does not match M1");
    }

    @Test
    @Disabled
    public void testInvalidPlayerReceiveHello() {
        /* ServerAuthenticationHandshake serverAuthenticationHandshake = new ServerAuthenticationHandshake(
                authenticationTable, authenticationConnectionHandler);

        String unsuccessfulHelloHandshake = serverAuthenticationHandshake.respondToHello(2);
        Assertions.assertEquals("bad_record_mac", unsuccessfulHelloHandshake); */
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        // empty
    }

}
