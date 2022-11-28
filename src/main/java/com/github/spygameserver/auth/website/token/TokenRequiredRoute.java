package com.github.spygameserver.auth.website.token;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.VerificationTokenTable;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
		// Parse the parameters either from the body (POST) or the query parameters (GET)
		JSONObject jsonObject = request.requestMethod().equals("POST") ? parseRequestIntoJSON(request.body())
				: parseRequestIntoJSON(request);

		if (!jsonObject.has("token")) {
			setErrorStatus(response);
			return null;
		}

		String token = jsonObject.getString("token");

		if (!jsonObject.has("email")) {
			setErrorStatus(response);
			return null;
		}

		String email = jsonObject.getString("email");

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
			setErrorStatus(response);
			return null;
		}

		// If we did not process the token correctly, then we do not want to delete it
		if (!processToken(jsonObject, playerId)) {
			setErrorStatus(response);
			return null;
		}

		// We successfully processed this token, so go ahead and delete it
		connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
		verificationTokenTable.deleteVerificationToken(connectionHandler, token);



		// Return the success message to the player
		return getSuccessMessage();
	}

	private JSONObject parseRequestIntoJSON(String body) {
		JSONObject jsonObject = new JSONObject();

		for (String keyValuePair : body.split("&")) {

			String[] keyValueSplit = keyValuePair.split("=");
			String key = keyValueSplit[0];
			String value = URLDecoder.decode(keyValueSplit[1], StandardCharsets.UTF_8);

			jsonObject.put(key, value);
		}

		return jsonObject;
	}

	private JSONObject parseRequestIntoJSON(Request request) {
		JSONObject jsonObject = new JSONObject();

		for (String key : request.queryParams()) {
			jsonObject.put(key, request.queryParams(key));
		}

		return jsonObject;
	}

	protected abstract boolean processToken(JSONObject jsonObject, int playerId);

	private void setErrorStatus(Response response) {
		response.status(ERROR_STATUS);
	}

	protected abstract String getSuccessMessage();

}
