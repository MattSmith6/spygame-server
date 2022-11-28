package com.github.spygameserver.auth.website.token;

import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.auth.website.token.TokenRequiredRoute;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Response;

public class ResetPasswordRoute extends TokenRequiredRoute {

    public ResetPasswordRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        super(gameDatabase, authenticationDatabase);
    }

    @Override
    protected boolean processToken(JSONObject jsonObject, int playerId) {
        String email = jsonObject.getString("email");

        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountData playerAccountData = gameDatabase.getPlayerAccountTable()
                .getPlayerAccountDataByEmail(connectionHandler, email);

        // Should never be null at this point, but avoid any potential errors here
        if (playerAccountData == null) {
            return false;
        }

        String username = playerAccountData.getUsername();

        if (!jsonObject.has("password")) {
            return false;
        }

        String password = jsonObject.getString("password");

        if (password == null || password.isEmpty()) {
            return false;
        }

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId, username, password);

        connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
        authenticationDatabase.getAuthenticationTable().updatePlayerAuthenticationRecord(connectionHandler,
                playerAuthenticationData);

        return true;
    }

    @Override
    protected String getSuccessMessage() {
        return "Successfully reset your password.";
    }

}
