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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerAccountTableTest implements DatabaseRequiredTest {

    private static final String TEST_VALID_EMAIL = "bob@my.csun.edu";
    private static final String TEST_VALID_USERNAME = "bobbi_boy123";

    private static final String TEST_INVALID_EMAIL = "phil@my.csun.edu";
    private static final String TEST_INVALID_USERNAME = "philly_cheesesteak";

    private GameDatabase gameDatabase;
    private PlayerAccountTable playerAccountTable;
    private ConnectionHandler connectionHandler;

    @BeforeAll
    public void setupConnection() {
        gameDatabase = getGameDatabase();

        playerAccountTable = gameDatabase.getPlayerAccountTable();
        connectionHandler = gameDatabase.getNewConnectionHandler(false);

        playerAccountTable.initialize(connectionHandler);
    }

    @Test
    public void testAllPaths() {
        if (!playerAccountTable.doesEmailAlreadyExist(connectionHandler, TEST_VALID_EMAIL)) {
            playerAccountTable.createPlayerAccount(connectionHandler, TEST_VALID_EMAIL, TEST_VALID_USERNAME);
        }

        checkValidAndInvalidUsernamesAfterInsert();
        checkAccountDataAfterUsernameFinished();

        checkAllDataMatchesWithEachOther();
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

        Assertions.assertEquals(AccountVerificationStatus.AWAITING_VERIFICATION,
                playerAccountData.getAccountVerificationStatus(),
                "Account status is not AWAITING_VERIFICATION after account created");
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
        closeOpenConnections(gameDatabase);
        closeOpenConnections(connectionHandler);
    }

}
