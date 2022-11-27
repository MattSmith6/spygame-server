package com.github.spygameserver.database.table;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VerificationTokenTableTest implements DatabaseRequiredTest {

	private static final int TEST_PLAYER_ID = 1234;

	private AuthenticationDatabase authenticationDatabase;
	private ConnectionHandler connectionHandler;

	@BeforeAll
	public void setupConnections() {
		authenticationDatabase = getAuthenticationDatabase();
		connectionHandler = authenticationDatabase.getNewConnectionHandler(false);
	}

	@Test
	public void testAllPaths() {
		VerificationTokenTable verificationTokenTable = authenticationDatabase.getVerificationTokenTable();
		verificationTokenTable.addNewVerificationTokenForPlayer(connectionHandler, TEST_PLAYER_ID);

		// Assert that we have a token for the player id after insert
		String verificationToken = verificationTokenTable.getVerificationTokenFromPlayerId(connectionHandler, TEST_PLAYER_ID);
		Assertions.assertNotNull(verificationToken);

		// Assert that the token's player id matches the id (redundant, but tests SQL query validity)
		int foundPlayerId = verificationTokenTable.getPlayerIdFromVerificationToken(connectionHandler, verificationToken);
		Assertions.assertEquals(TEST_PLAYER_ID, foundPlayerId);

		verificationTokenTable.deleteVerificationToken(connectionHandler, verificationToken);

		// Update the token after deletion, assert that it is null for this player
		verificationToken = verificationTokenTable.getVerificationTokenFromPlayerId(connectionHandler, TEST_PLAYER_ID);
		Assertions.assertNull(verificationToken);
	}


	@AfterAll
	@Override
	public void closeOpenConnections() {
		closeOpenConnections(authenticationDatabase);
	}

}
