package com.github.spygameserver.database.table;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.caesar.Hex;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.ByteOrder;

/**
 * Tests all the paths associated with the AuthenticationTable class. Ensures that any errors in SQL are caught before
 * putting the system on the server.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerAuthenticationTableTest implements DatabaseRequiredTest {

    private static final int TEST_PLAYER_ID = 101;

    private AuthenticationDatabase authenticationDatabase;
    private PlayerAuthenticationTable authenticationTable;
    private ConnectionHandler connectionHandler;

    /**
     * Establishes a connection to the database.
     */
    @BeforeAll
    public void setupAuthenticationTable() {
        authenticationDatabase = getAuthenticationDatabase();

        authenticationTable = authenticationDatabase.getAuthenticationTable();
        connectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        authenticationTable.initialize(connectionHandler);
    }

    /**
     * Tests all paths that are used in the AuthenticationTable class.
     */
    @Test
    public void testAllPaths() {
        createAndVerifyAuthenticationData();
        updateAndVerifyAuthenticationData();
    }

    /**
     * Creates and verifies that a record matches expected insertion data in the authentication table.
     */
    private void createAndVerifyAuthenticationData() {
        PlayerAuthenticationData fetchedData = authenticationTable.getPlayerAuthenticationRecord(connectionHandler, TEST_PLAYER_ID);
        PlayerAuthenticationData testInsertData = new PlayerAuthenticationData(TEST_PLAYER_ID,
                getExampleSalt('A'), getExampleVerifier('B'));

        // If the data does not already exist in the database for this id, we need to add it, or else update it
        if (fetchedData == null) {
            authenticationTable.addPlayerAuthenticationRecord(connectionHandler, testInsertData);
        } else {
            authenticationTable.updatePlayerAuthenticationRecord(connectionHandler, testInsertData);
        }

        PlayerAuthenticationData retrievedPlayerAuthenticationData = authenticationTable
                .getPlayerAuthenticationRecord(connectionHandler, TEST_PLAYER_ID);

        // Assert that the data retrieved after add/update matches the data we inserted
        Assertions.assertEquals(testInsertData, retrievedPlayerAuthenticationData,
                "Data inserted into the database does not match data retrieved from the database.");
    }

    /**
     * Updates and verifies that a record matches expected update data in the authentication database.
     */
    private void updateAndVerifyAuthenticationData() {
        // Update the data we just inserted to be different values
        PlayerAuthenticationData updatedPlayerAuthenticationData = new PlayerAuthenticationData(TEST_PLAYER_ID,
                getExampleSalt('C'), getExampleVerifier('D'));
        authenticationTable.updatePlayerAuthenticationRecord(connectionHandler, updatedPlayerAuthenticationData);

        PlayerAuthenticationData retrievedPlayerAuthenticationData = authenticationTable
                .getPlayerAuthenticationRecord(connectionHandler, TEST_PLAYER_ID);

        // Assert that the data retrieved after update matches the data we expect
        Assertions.assertEquals(updatedPlayerAuthenticationData, retrievedPlayerAuthenticationData,
                "Data updated in the database does not match data retrieved from the database.");
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(authenticationDatabase);
        closeOpenConnections(connectionHandler);
    }

    /**
     * Generates the SRP6-Variable in byte form to match the salt parameter
     * @param c the character to repeat
     * @return the Bytes object representing a salt with the same repeated character
     */
    private Bytes getExampleSalt(char c) {
        return repeatCharacter(c, 64);
    }

    /**
     * Generates the SRP6-Variable in SRP6IntegerVariable form to match the verifier parameter
     * @param c the character to repeat
     * @return the SRP6IntegerVariable object representing a verifier with the same repeated character
     */
    private SRP6IntegerVariable getExampleVerifier(char c) {
        return new SRP6CustomIntegerVariable(repeatCharacter(c, 256), ByteOrder.BIG_ENDIAN);
    }

    /**
     * Generates a string of a character, c, that has a length of numTimes
     * @param c the character to repeat
     * @param numTimes the number of times the character should be repeated
     * @return the Hex version of the string that was generated
     */
    private Hex repeatCharacter(char c, int numTimes) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 1; i <= numTimes; i++) {
            stringBuilder.append(c);
        }

        String bytesStringForm = stringBuilder.toString();
        return new Hex(bytesStringForm);
    }

}
