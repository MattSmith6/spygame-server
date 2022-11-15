package com.github.spygameserver.auth.website;

import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Response;

public class AddUsernameEmailRoute extends VerificationRoute {

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    public AddUsernameEmailRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;
    }

    @Override
    public JSONObject handleAdditional(JSONObject requestBody, Response response) {
        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountTable playerAccountTable = gameDatabase.getPlayerAccountTable();

        String email = requestBody.getString("email");

        PlayerAccountData playerAccountData = playerAccountTable.getPlayerAccountDataByEmail(connectionHandler, email);

        // If there is no record in the game table at all, there is no account (first step is not finished)
        if (playerAccountData == null) {
            return getErrorObject("No account has been created for this email address.");
        }

        String usernameInTable = playerAccountData.getUsername();

        // If the table has something other than null, we don't want to overwrite their chosen username
        if (usernameInTable != null) {
            return getErrorObject("This account has already chosen a username.");
        }

        String username = requestBody.getString("username");

        // If the account table already exists
        if (playerAccountTable.doesUsernameAlreadyExist(connectionHandler, username)) {
            return getErrorObject("This username already exists for another account.");
        }

        connectionHandler.closeAbsolutely();

        int playerId = playerAccountData.getPlayerId();
        String password = requestBody.getString("password");

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId, username, password);

        // Add authentication information to the database
        connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
        authenticationDatabase.getAuthenticationTable().addPlayerAuthenticationRecord(connectionHandler,
                playerAuthenticationData);

        connectionHandler.closeAbsolutely();

        return getSuccessObject();
    }

}
