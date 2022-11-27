package com.github.spygameserver.auth.website.token;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.VerificationTokenTable;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class TokenRequiredRoute implements Route {

	private static final int ERROR_STATUS = 400;

	protected final GameDatabase gameDatabase;
	protected final AuthenticationDatabase authenticationDatabase;

	protected TokenRequiredRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
		this.gameDatabase = gameDatabase;
		this.authenticationDatabase = authenticationDatabase;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		String token = request.queryParams("token");

		if (token == null) {
			setErrorStatus(response);
			return null;
		}

		String email = request.queryParams("email");

		if (email == null) {
			setErrorStatus(response);
			return null;
		}

		ConnectionHandler connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
		VerificationTokenTable verificationTokenTable = authenticationDatabase.getVerificationTokenTable();

		Integer playerId = verificationTokenTable.getPlayerIdFromVerificationToken(connectionHandler, token);

		if (playerId == null) {
			setErrorStatus(response);
			return null;
		}

		connectionHandler = gameDatabase.getNewConnectionHandler(true);
		PlayerAccountData playerAccountData = gameDatabase.getPlayerAccountTable().getPlayerAccountDataByEmail(connectionHandler, email);

		// If the specified token does not match the email for the player, error
		if (playerAccountData.getPlayerId() != playerId) {
			response.status(ERROR_STATUS);
			return null;
		}

		// Our token and email match, so delete the token and process it
		connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
		verificationTokenTable.deleteVerificationToken(connectionHandler, token);

		// Update the account's verification status to be the desired status
		connectionHandler = gameDatabase.getNewConnectionHandler(true);
		gameDatabase.getPlayerAccountTable().updatePlayerVerificationStatus(connectionHandler,
				getDesiredAccountVerificationStatus(), playerId);

		// Return the success message to the player
		return getSuccessMessage();
	}

	private void setErrorStatus(Response response) {
		response.status(ERROR_STATUS);
	}

	protected abstract AccountVerificationStatus getDesiredAccountVerificationStatus();

	protected abstract String getSuccessMessage();

}
