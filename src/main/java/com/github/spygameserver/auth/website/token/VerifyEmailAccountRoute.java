package com.github.spygameserver.auth.website.token;

import com.github.spygameserver.auth.website.email.EmailRequiredRoute;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import org.json.JSONObject;
import spark.Response;

public class VerifyEmailAccountRoute extends TokenRequiredRoute {

	public VerifyEmailAccountRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
		super(gameDatabase, authenticationDatabase);
	}

	@Override
	protected AccountVerificationStatus getDesiredAccountVerificationStatus() {
		return AccountVerificationStatus.VERIFIED;
	}

	@Override
	protected String getSuccessMessage() {
		return "Account successfully verified. Login to SpyGame to start playing.";
	}

}
