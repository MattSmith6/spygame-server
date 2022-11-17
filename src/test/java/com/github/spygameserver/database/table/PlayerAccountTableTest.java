package com.github.spygameserver.database.table;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerAccountTableTest implements DatabaseRequiredTest {

    private static final String TEST_VALID_EMAIL = "bob@my.csun.edu";
    private static final String TEST_VALID_USERNAME = "bobbi_boy123";

    private static final String TEST_INVALID_EMAIL = "phil@my.csun.edu";
    private static final String TEST_INVALID_USERNAME = "philly_cheesesteak";

    private PlayerAccountTable playerAccountTable;
    private ConnectionHandler connectionHandler;

    @BeforeAll
    public void setupConnection() {
        GameDatabase gameDatabase = getGameDatabase();

        playerAccountTable = gameDatabase.getPlayerAccountTable();
        connectionHandler = gameDatabase.getNewConnectionHandler(false);

        // Make sure no data persists, since these fields need to be unique
        playerAccountTable.dropTableSecure(connectionHandler);
        playerAccountTable.initialize(connectionHandler);
    }

    @Test
    public void testAllPaths() {
        playerAccountTable.addVerifiedEmail(connectionHandler, TEST_VALID_EMAIL);

        checkValidAndInvalidEmailsAfterInsert();
        checkAccountDataAfterEmailOnly();

        playerAccountTable.addUsernameToPlayerAccount(connectionHandler, TEST_VALID_EMAIL, TEST_VALID_USERNAME);

        checkValidAndInvalidUsernamesAfterInsert();
        checkAccountDataAfterUsernameFinished();

        checkAllDataMatchesWithEachOther();
    }

    private void checkValidAndInvalidEmailsAfterInsert() {
        boolean hasValidEmailInserted = playerAccountTable.doesEmailAlreadyExist(connectionHandler, TEST_VALID_EMAIL);
        boolean hasInvalidEmailInserted = playerAccountTable.doesEmailAlreadyExist(connectionHandler, TEST_INVALID_EMAIL);

        Assertions.assertTrue(hasValidEmailInserted);
        Assertions.assertFalse(hasInvalidEmailInserted);
    }

    private void checkAccountDataAfterEmailOnly() {
        PlayerAccountData playerAccountData = playerAccountTable.getPlayerAccountDataByEmail(connectionHandler,
                TEST_VALID_EMAIL);

        Assertions.assertEquals(TEST_VALID_EMAIL, playerAccountData.getEmail(),
                "Email does not match for after email only insert to the database");

        Assertions.assertNull(playerAccountData.getUsername(), "Username is not null after email only insertion");

        Assertions.assertEquals(AccountVerificationStatus.CHOOSE_USERNAME,
                playerAccountData.getAccountVerificationStatus(),
                "Account status is not CHOOSE_USERNAME when inserting only the email into database");
    }

    private void checkValidAndInvalidUsernamesAfterInsert() {
        boolean hasValidUsernameInserted = playerAccountTable.doesUsernameAlreadyExist(connectionHandler,
                TEST_VALID_USERNAME);
        boolean hasInvalidUsernameInserted = playerAccountTable.doesUsernameAlreadyExist(connectionHandler,
                TEST_INVALID_USERNAME);

        Assertions.assertTrue(hasValidUsernameInserted);
        Assertions.assertFalse(hasInvalidUsernameInserted);
    }

    private void checkAccountDataAfterUsernameFinished() {
        PlayerAccountData playerAccountData = playerAccountTable.getPlayerAccountDataByEmail(connectionHandler,
                TEST_VALID_EMAIL);

        Assertions.assertEquals(TEST_VALID_EMAIL, playerAccountData.getEmail(),
                "Email is not inserted for this record in the database");

        Assertions.assertEquals(TEST_VALID_USERNAME, playerAccountData.getUsername(),
                "Username does not match after updated record");

        Assertions.assertEquals(AccountVerificationStatus.VERIFIED,
                playerAccountData.getAccountVerificationStatus(),
                "Account status is not VERIFIED after updated record");
    }

    private void checkAllDataMatchesWithEachOther() {
        PlayerAccountData playerAccountDataByEmail = playerAccountTable.getPlayerAccountDataByEmail(connectionHandler,
                TEST_VALID_EMAIL);

        int playerId = playerAccountDataByEmail.getPlayerId();
        PlayerAccountData playerAccountDataById = playerAccountTable.getPlayerAccountData(connectionHandler, playerId);

        Assertions.assertEquals(playerAccountDataByEmail, playerAccountDataById,
                "Player account data does not match from different select queries");

        PlayerVerificationData verificationDataFromAccountData = new PlayerVerificationData(playerId,
                playerAccountDataByEmail.getAccountVerificationStatus());
        PlayerVerificationData verificationDataFromTable = playerAccountTable.getPlayerVerificationInfo(connectionHandler,
                TEST_VALID_USERNAME);

        Assertions.assertEquals(verificationDataFromAccountData, verificationDataFromTable,
                "Player verification data does not match from different methods of retrieval");
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(connectionHandler);
    }

}
