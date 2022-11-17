package com.github.spygameserver.auth;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.caesar.Hex;
import com.github.glusk.caesar.PlainText;
import com.github.glusk.caesar.hashing.ImmutableMessageDigest;
import com.github.glusk.srp6_variables.SRP6ClientPublicKey;
import com.github.glusk.srp6_variables.SRP6ClientSessionProof;
import com.github.glusk.srp6_variables.SRP6ClientSharedSecret;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6Exception;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.glusk.srp6_variables.SRP6Multiplier;
import com.github.glusk.srp6_variables.SRP6PrivateKey;
import com.github.glusk.srp6_variables.SRP6RandomEphemeral;
import com.github.glusk.srp6_variables.SRP6ScramblingParameter;
import com.github.glusk.srp6_variables.SRP6ServerSessionProof;
import com.github.glusk.srp6_variables.SRP6SessionKey;
import com.github.glusk.srp6_variables.SRP6Verifier;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

import static com.github.spygameserver.auth.ServerAuthenticationHandshake.N;
import static com.github.spygameserver.auth.ServerAuthenticationHandshake.g;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerAuthenticationHandshakeTest implements DatabaseRequiredTest {

    private static final String EMAIL = "alice_fake@my.csun.edu";
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "password123";

    private static final Bytes USERNAME_BYTES = new PlainText(USERNAME);
    private static final Bytes PASSWORD_BYTES = new PlainText(PASSWORD);

    public static final Logger LOG = Logger.getLogger(ServerAuthenticationHandshakeTest.class.getName());

    private GameDatabase gameDatabase;
    private AuthenticationDatabase authenticationDatabase;

    private int realPlayerId;

    @BeforeAll
    public void setupDatabaseConnection() {
        authenticationDatabase = getAuthenticationDatabase();

        ConnectionHandler connectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        authenticationDatabase.getAuthenticationTable().dropTableSecure(connectionHandler);
        authenticationDatabase.getAuthenticationTable().createTableIfNotExists(connectionHandler);

        connectionHandler.closeAbsolutely();

        gameDatabase = getGameDatabase();

        insertTestDataIfNecessary();
    }

    public void insertTestDataIfNecessary() {
        PlayerAccountTable playerAccountTable = gameDatabase.getPlayerAccountTable();
        ConnectionHandler gameConnectionHandler = gameDatabase.getNewConnectionHandler(false);

        PlayerAccountData playerAccountData = playerAccountTable.getPlayerAccountDataByEmail(gameConnectionHandler, EMAIL);

        if (playerAccountData == null) {
            playerAccountTable.addVerifiedEmail(gameConnectionHandler, EMAIL);
            playerAccountTable.addUsernameToPlayerAccount(gameConnectionHandler, EMAIL, USERNAME);
        }

        int playerId = playerAccountTable.getPlayerIdByUsername(gameConnectionHandler, USERNAME);
        gameConnectionHandler.closeAbsolutely();

        AuthenticationTable authenticationTable = authenticationDatabase.getAuthenticationTable();
        ConnectionHandler authConnectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        // If the table our data already exists, we don't need to be here
        if (authenticationTable.getPlayerAuthenticationRecord(authConnectionHandler, playerId) != null) {
            authConnectionHandler.closeAbsolutely();
            return;
        }

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId,
                USERNAME, PASSWORD);

        authenticationTable.addPlayerAuthenticationRecord(authConnectionHandler, playerAuthenticationData);

        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        realPlayerId = gameDatabase.getPlayerAccountTable().getPlayerIdByUsername(connectionHandler, USERNAME);

        authConnectionHandler.closeAbsolutely();
    }

    @Test
    public void testInvalidPlayerIdReceiveHello() {
        ServerAuthenticationHandshake handshake = new ServerAuthenticationHandshake(USERNAME, authenticationDatabase);
        JSONObject responseToPlayerHello = handshake.respondToHello(realPlayerId + 1);

        String errorMessage = getErrorMessage(responseToPlayerHello);
        Assertions.assertEquals("bad_record_mac", errorMessage);
    }

    @Test
    public void testInvalidKeyExchangePlayerReceiveHello() {
        ServerAuthenticationHandshake handshake = new ServerAuthenticationHandshake(USERNAME, authenticationDatabase);
        JSONObject responseToPlayerHello = handshake.respondToHello(realPlayerId);

        if (responseToPlayerHello.has("error")) {
            Assertions.fail(getErrorMessage(responseToPlayerHello));
        }

        ClientKeyExchange clientKeyExchange = generateClientKeyExchange(responseToPlayerHello);

        SRP6IntegerVariable A = clientKeyExchange.A;

        // Modify M1 to create a mismatch from valid generation
        byte[] m1Shenanigans = clientKeyExchange.M1.asArray();
        m1Shenanigans[0] = (byte) (m1Shenanigans[0] - 1);

        Bytes M1 = Bytes.wrapped(m1Shenanigans);

        JSONObject responseToKeyExchange = handshake.respondToKeyExchange(A, M1);
        String error = responseToKeyExchange.getString("error");

        Assertions.assertEquals("Client proof mismatch!", error);
    }

    private Bytes getBytesFromJSON(JSONObject jsonObject, String path) {
        String encodedString = jsonObject.getString(path);
        return Bytes.wrapped(Base64.getDecoder().decode(encodedString));
    }

    private SRP6IntegerVariable getIntegerVariableFromJSON(JSONObject jsonObject, String path) {
        return new SRP6CustomIntegerVariable(getBytesFromJSON(jsonObject, path), ByteOrder.BIG_ENDIAN);
    }

    @Test
    public void testValidPlayerReceiveHello() {
        ServerAuthenticationHandshake handshake = new ServerAuthenticationHandshake(USERNAME, authenticationDatabase);
        JSONObject responseToPlayerHello = handshake.respondToHello(realPlayerId);

        if (responseToPlayerHello.has("error")) {
            Assertions.fail(getErrorMessage(responseToPlayerHello));
        }

        ClientKeyExchange clientKeyExchange = generateClientKeyExchange(responseToPlayerHello);

        SRP6IntegerVariable A = clientKeyExchange.A;
        Bytes M1 = clientKeyExchange.M1;

        JSONObject responseToKeyExchange = handshake.respondToKeyExchange(A, M1);

        if (responseToKeyExchange.has("error")) {
            Assertions.fail(getErrorMessage(responseToKeyExchange));
        }

        Bytes M2 = getBytesFromJSON(responseToKeyExchange, "M2");

        // Verify that M1 proof matches M2 proof
        try {
            Bytes cM2 = new SRP6ServerSessionProof(ServerAuthenticationHandshake.IMD, N, A, M1, clientKeyExchange.K,
                    ByteOrder.BIG_ENDIAN);
            if (!(cM2.equals(M2))) {
                throw new SRP6Exception("Server proof mismatch!");
            }
        } catch (SRP6Exception e) {
            Assertions.fail("Server proof mismatch!");
        }
    }

    private ClientKeyExchange generateClientKeyExchange(JSONObject responseToPlayerHello) {
        SecureRandom rng = new SecureRandom();
        ImmutableMessageDigest IMD = ServerAuthenticationHandshake.IMD;
        ByteOrder byteOrder = ServerAuthenticationHandshake.BYTE_ORDER;

        SRP6IntegerVariable N = getIntegerVariableFromJSON(responseToPlayerHello, "N");
        SRP6IntegerVariable g = getIntegerVariableFromJSON(responseToPlayerHello, "g");
        Bytes s = getBytesFromJSON(responseToPlayerHello, "s");
        SRP6IntegerVariable B = getIntegerVariableFromJSON(responseToPlayerHello, "B");

        try {
            SRP6IntegerVariable x = new SRP6PrivateKey(ServerAuthenticationHandshake.IMD, s, USERNAME_BYTES, PASSWORD_BYTES,
                    ServerAuthenticationHandshake.BYTE_ORDER);

            SRP6IntegerVariable a = new SRP6RandomEphemeral(rng, -1, N);
            SRP6IntegerVariable A = new SRP6ClientPublicKey(N, g, a);
            SRP6IntegerVariable u = new SRP6ScramblingParameter(IMD, A, B, N, byteOrder);

            SRP6IntegerVariable k = new SRP6Multiplier(IMD, N, g, byteOrder);

            SRP6IntegerVariable S = new SRP6ClientSharedSecret(N, g, k, B, x, u, a);
            Bytes K = new SRP6SessionKey(IMD, S, byteOrder);
            Bytes M1 = new SRP6ClientSessionProof(IMD, N, g, USERNAME_BYTES, s, A, B, K, byteOrder);

            ClientKeyExchange clientKeyExchange = new ClientKeyExchange();

            clientKeyExchange.A = A;
            clientKeyExchange.M1 = M1;
            clientKeyExchange.K = K;
            clientKeyExchange.S = S;

            return clientKeyExchange;
        } catch (SRP6Exception e) {
            Assertions.fail("Client mismatch.");
        }

        return null;
    }

    private String getErrorMessage(JSONObject jsonObject) {
        return jsonObject.getString("error");
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        // empty
    }

    private class ClientKeyExchange {

        public SRP6IntegerVariable A;
        public Bytes M1;
        public Bytes K;
        public SRP6IntegerVariable S;

    }

}
