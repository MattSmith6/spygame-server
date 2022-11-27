package com.github.spygameserver.auth.website.token;

import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;

public class DisablePlayerAccountRoute extends TokenRequiredRoute {

	public DisablePlayerAccountRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
		super(gameDatabase, authenticationDatabase);
	}

	@Override
	protected AccountVerificationStatus getDesiredAccountVerificationStatus() {
		return AccountVerificationStatus.DISABLED;
	}

	@Override
	protected String getSuccessMessage() {
		return "Your account has been disabled. Register an account with the same email to unfreeze the account.";
	}

}
