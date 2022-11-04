package com.github.spygameserver.auth;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.DatabaseCredentialsProcessor;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.table.AuthenticationTable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServerAuthenticationHandshakeTest {

    private static final String FILE_RESOURCE_PATH = "src/test/resources";
    private static final String DATABASE_CREDENTIALS_FILE = "database_credentials.properties";

    private boolean isSetup = true;
    private String errorOnSetupMessage = null;

    private AuthenticationDatabase authenticationDatabase;
    private AuthenticationTable authenticationTable;

    private ConnectionHandler authenticationConnectionHandler;

    @BeforeAll
    public void setupDatabaseConnection() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String databaseCredentialsFilePath = classLoader.getResource(DATABASE_CREDENTIALS_FILE).getFile();
        File databaseCredentialsFile = new File(databaseCredentialsFilePath);

        DatabaseCredentialsProcessor databaseCredentialsProcessor = new DatabaseCredentialsProcessor(
                databaseCredentialsFile, "auth_db"
        );

        if (!databaseCredentialsProcessor.didFileExistOnStartup()) {
            isSetup = false;
            errorOnSetupMessage = String.format("Database credentials file did not exist. Please configure the %s file in " +
                    "%s and run again.", DATABASE_CREDENTIALS_FILE, FILE_RESOURCE_PATH);
            return;
        }

        DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager(databaseCredentialsProcessor);
        authenticationDatabase = new AuthenticationDatabase(databaseConnectionManager, true);
        authenticationTable = authenticationDatabase.getAuthenticationTable();

        authenticationConnectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        if (authenticationDatabase.getAuthenticationTable() == null) {
            isSetup = false;
            errorOnSetupMessage = "Could not establish connection to database.";
        }

        insertTestDataIfNecessary();
    }

    private void insertTestDataIfNecessary() {
        boolean hasCreatedAnyTables = authenticationDatabase.hasCreatedAnyTables();

        // We don't need to insert any data, the tables already exist, so we can exit early
        if (hasCreatedAnyTables) {
            return;
        }

        int examplePlayerId = 1;
        String exampleSalt = removeAllWhitespaces("BEB25379 D1A8581E B5A72767 3A2441EE");
        String exampleVerifier = removeAllWhitespaces(
                "7E273DE8 696FFC4F 4E337D05 B4B375BE B0DDE156 9E8FA00A 9886D812\n" +
                "9BADA1F1 822223CA 1A605B53 0E379BA4 729FDC59 F105B478 7E5186F5\n" +
                "C671085A 1447B52A 48CF1970 B4FB6F84 00BBF4CE BFBB1681 52E08AB5\n" +
                "EA53D15C 1AFF87B2 B9DA6E04 E058AD51 CC72BFC9 033B564E 26480D78\n" +
                "E955A5E2 9E7AB245 DB2BE315 E2099AFB"
        );

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(examplePlayerId,
                exampleSalt, exampleVerifier);

        authenticationTable.addPlayerAuthenticationRecord(authenticationConnectionHandler, playerAuthenticationData);
    }

    private String removeAllWhitespaces(String string) {
        return string.replace(" ", "").replace("\n", "");
    }

    private void failIfNotSetup() {
        if (!isSetup) {
            Assertions.fail(errorOnSetupMessage);
        }
    }

    @Test
    public void testPlayerId() {
        failIfNotSetup();

        ServerAuthenticationHandshake serverAuthenticationHandshake = new ServerAuthenticationHandshake(
                null, authenticationTable, authenticationConnectionHandler);

        String successfulHelloHandshake = serverAuthenticationHandshake.receiveHello(1);
        Assertions.assertEquals(successfulHelloHandshake, "Success!");

        String unsuccessfulHelloHandshake = serverAuthenticationHandshake.receiveHello(2);
        Assertions.assertEquals(unsuccessfulHelloHandshake, "bad_record_mac");
    }

    @AfterAll
    public void closeDatabaseConnections() {
        authenticationConnectionHandler.setShouldCloseConnectionAfterUse(true);
        authenticationConnectionHandler.closeConnectionIfNecessary();
    }

}
