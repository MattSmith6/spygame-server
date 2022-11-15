package com.github.spygameserver.auth;

import com.github.glusk.caesar.Hex;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.table.AuthenticationTable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.ByteOrder;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerAuthenticationHandshakeTest implements DatabaseRequiredTest {

    public static final Logger LOG = Logger.getLogger(ServerAuthenticationHandshakeTest.class.getName());

    private AuthenticationTable authenticationTable;
    private ConnectionHandler authenticationConnectionHandler;

    @BeforeAll
    public void setupDatabaseConnection() {
        File databaseCredentialsFile = getValidCredentialsFile();

        DatabaseCreator<AuthenticationDatabase> authenticationDatabaseCreator = new DatabaseCreator<>(
                databaseCredentialsFile, "auth_db", true);
        AuthenticationDatabase authenticationDatabase = authenticationDatabaseCreator
                .createDatabaseFromFile(AuthenticationDatabase::new);

        authenticationTable = authenticationDatabase.getAuthenticationTable();
        authenticationConnectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        insertTestDataIfNecessary();
    }

    public void insertTestDataIfNecessary() {
        // If the table our data already exists, we don't need to be here
        if (authenticationTable.getPlayerAuthenticationRecord(authenticationConnectionHandler, 1) != null) {
            return;
        }

        int examplePlayerId = 1;
        SRP6CustomIntegerVariable exampleSalt = getIntegerVariableFromByteString("BEB25379 D1A8581E B5A72767 3A2441EE");
        SRP6CustomIntegerVariable exampleVerifier = getIntegerVariableFromByteString(
                "7E273DE8 696FFC4F 4E337D05 B4B375BE B0DDE156 9E8FA00A 9886D812" +
                "9BADA1F1 822223CA 1A605B53 0E379BA4 729FDC59 F105B478 7E5186F5" +
                "C671085A 1447B52A 48CF1970 B4FB6F84 00BBF4CE BFBB1681 52E08AB5" +
                "EA53D15C 1AFF87B2 B9DA6E04 E058AD51 CC72BFC9 033B564E 26480D78" +
                "E955A5E2 9E7AB245 DB2BE315 E2099AFB"
        );

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(examplePlayerId,
                exampleSalt, exampleVerifier);

        authenticationTable.addPlayerAuthenticationRecord(authenticationConnectionHandler, playerAuthenticationData);
    }

    private SRP6CustomIntegerVariable getIntegerVariableFromByteString(String bytes) {
        return new SRP6CustomIntegerVariable(new Hex(bytes), ByteOrder.BIG_ENDIAN);
    }

    @Test
    public void testValidPlayerReceiveHello() {
        ServerAuthenticationHandshake serverAuthenticationHandshake = new ServerAuthenticationHandshake(
                authenticationTable, authenticationConnectionHandler);

        String successfulHelloHandshake = serverAuthenticationHandshake.receiveHello(1);
        Assertions.assertEquals("Success!", successfulHelloHandshake);
    }

    @Test
    public void testInvalidPlayerReceiveHello() {
        ServerAuthenticationHandshake serverAuthenticationHandshake = new ServerAuthenticationHandshake(
                authenticationTable, authenticationConnectionHandler);

        String unsuccessfulHelloHandshake = serverAuthenticationHandshake.receiveHello(2);
        Assertions.assertEquals("bad_record_mac", unsuccessfulHelloHandshake);
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(authenticationConnectionHandler);
    }

}
