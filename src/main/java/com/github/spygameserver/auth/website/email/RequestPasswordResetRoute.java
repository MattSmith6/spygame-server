package com.github.spygameserver.auth.website.email;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.email.ResetPasswordEmailCreator;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.apache.commons.mail.EmailException;
import org.json.JSONObject;
import spark.Response;

// Will return a success object if: a player account does not exist, or if a player account does exist and is verified
// Will return an error object if: a player account exists but is not yet verified
public class RequestPasswordResetRoute extends EmailRequiredRoute {

	private final GameDatabase gameDatabase;
	private final AuthenticationDatabase authenticationDatabase;

	public RequestPasswordResetRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
		this.gameDatabase = gameDatabase;
		this.authenticationDatabase = authenticationDatabase;
	}

	@Override
	public JSONObject handleAdditional(JSONObject jsonObject, String email, Response response) {
		ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
		PlayerAccountData playerAccountData = gameDatabase.getPlayerAccountTable()
				.getPlayerAccountDataByEmail(connectionHandler, email);

		// Should never be null at this point, but avoid any potential errors here
		if (playerAccountData == null) {
			return getSuccessObject();
		}

		if (playerAccountData.getAccountVerificationStatus() != AccountVerificationStatus.VERIFIED) {
			return getErrorObject("Verify your account before attempting to reset your password.");
		}

		connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
		String verificationToken = authenticationDatabase.getVerificationTokenTable()
				.addNewVerificationTokenForPlayer(connectionHandler, playerAccountData.getPlayerId());

		try {
			new ResetPasswordEmailCreator(playerAccountData.getEmail(), verificationToken).sendNewEmail();
		} catch (EmailException ex) {
			ex.printStackTrace();
		}

		return getSuccessObject();
	}

}
