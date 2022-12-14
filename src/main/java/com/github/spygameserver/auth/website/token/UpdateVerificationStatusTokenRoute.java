package com.github.spygameserver.auth.website.token;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import org.json.JSONObject;

/**
 * A Route that requires a token to be used whose only purpose is to update the verification status if the token is valid.
 */
public abstract class UpdateVerificationStatusTokenRoute extends TokenRequiredRoute {

	protected UpdateVerificationStatusTokenRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
		super(gameDatabase, authenticationDatabase);
	}

	@Override
	protected boolean processToken(JSONObject jsonObject, int playerId) {
		// Update the account's verification status to be the desired status
		ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
		gameDatabase.getPlayerAccountTable().updatePlayerVerificationStatus(connectionHandler,
				getDesiredAccountVerificationStatus(), playerId);

		return true;
	}

	/**
	 * The verification status to update to for the subclass
	 * @return The verification status to update to for the subclass
	 */
	protected abstract AccountVerificationStatus getDesiredAccountVerificationStatus();

}
