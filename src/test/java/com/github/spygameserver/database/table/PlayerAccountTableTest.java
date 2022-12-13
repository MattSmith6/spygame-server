package com.github.spygameserver.database.table;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests all paths associated with the PlayerAccountTable. Ensures that SQL
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerAccountTableTest implements DatabaseRequiredTest {

    private static final String TEST_VALID_EMAIL = "bob@my.csun.edu";
    private static final String TEST_VALID_USERNAME = "bobbi_boy123";

    private static final String TEST_INVALID_EMAIL = "phil@my.csun.edu";
    private static final String TEST_INVALID_USERNAME = "philly_cheesesteak";

    private GameDatabase gameDatabase;
    private PlayerAccountTable playerAccountTable;
    private ConnectionHandler connectionHandler;

    /**
     * Setup a connection to the databases for this test.
     */
    @BeforeAll
    public void setupConnection() {
        gameDatabase = getGameDatabase();

        playerAccountTable = gameDatabase.getPlayerAccountTable();
        connectionHandler = gameDatabase.getNewConnectionHandler(false);

        insertTestDataIfNotExists();
    }

    public void insertTestDataIfNotExists() {
        // If the test data does not already exist, insert it into the database
        if (!playerAccountTable.doesEmailAlreadyExist(connectionHandler, TEST_VALID_EMAIL)) {
            playerAccountTable.createPlayerAccount(connectionHandler, TEST_VALID_EMAIL, TEST_VALID_USERNAME);
        }
    }

    /**
     * Check to see if the doesUsernameAlreadyExist function work for both a valid and invalid username.
     */
    @Test
    public void checkValidAndInvalidUsernamesAfterInsert() {
        // Check if
        boolean hasValidUsernameInserted = playerAccountTable.doesUsernameAlreadyExist(connectionHandler,
                TEST_VALID_USERNAME);
        boolean hasInvalidUsernameInserted = playerAccountTable.doesUsernameAlreadyExist(connectionHandler,
                TEST_INVALID_USERNAME);

        Assertions.assertTrue(hasValidUsernameInserted);
        Assertions.assertFalse(hasInvalidUsernameInserted);
    }

    /**
     * Check to see if the doesEmailAlreadyExist function work for both a valid and invalid email.
     */
    @Test
    public void checkValidAndInvalidEmailsAfterInsert() {
        boolean hasValidEmailInserted = playerAccountTable.doesEmailAlreadyExist(connectionHandler,
                TEST_VALID_EMAIL);
        boolean hasInvalidEmailInserted = playerAccountTable.doesEmailAlreadyExist(connectionHandler,
                TEST_INVALID_EMAIL);

        Assertions.assertTrue(hasValidEmailInserted);
        Assertions.assertFalse(hasInvalidEmailInserted);
    }

    /**
     * Checks to see that the data for an account was entered correctly in the database.
     */
    @Test
    public void checkAccountDataValidity() {
        PlayerAccountData playerAccountData = playerAccountTable.getPlayerAccountDataByEmail(connectionHandler,
                TEST_VALID_EMAIL);

        Assertions.assertEquals(TEST_VALID_EMAIL, playerAccountData.getEmail(),
                "Email is not inserted for this record in the database");

        Assertions.assertEquals(TEST_VALID_USERNAME, playerAccountData.getUsername(),
                "Username does not match after updated record");

        Assertions.assertEquals(AccountVerificationStatus.AWAITING_VERIFICATION,
                playerAccountData.getAccountVerificationStatus(),
                "Account status is not AWAITING_VERIFICATION after account created");
    }

    /**
     * Checks to see that multiple functions, when returning subsets of the account data, match the expected data.
     */
    @Test
    public void checkAllDataMatchesWithEachOther() {
        PlayerAccountData playerAccountDataByEmail = playerAccountTable.getPlayerAccountDataByEmail(connectionHandler,
                TEST_VALID_EMAIL);

        int playerId = playerAccountDataByEmail.getPlayerId();
        PlayerAccountData playerAccountDataById = playerAccountTable.getPlayerAccountData(connectionHandler, playerId);

        Assertions.assertEquals(playerAccountDataByEmail, playerAccountDataById,
                "Player account data does not match from different select queries");

        PlayerVerificationData verificationDataFromAccountData = new PlayerVerificationData(playerId,
                playerAccountDataByEmail.getAccountVerificationStatus());
        PlayerVerificationData verificationDataFromTable = playerAccountTable.getPlayerVerificationData(connectionHandler,
                TEST_VALID_USERNAME);

        Assertions.assertEquals(verificationDataFromAccountData, verificationDataFromTable,
                "Player verification data does not match from different methods of retrieval");
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(gameDatabase);
        closeOpenConnections(connectionHandler);
    }

}
