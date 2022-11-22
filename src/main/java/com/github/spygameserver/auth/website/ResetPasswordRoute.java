package com.github.spygameserver.auth.website;

import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Response;

import java.util.Map;

public class ResetPasswordRoute extends VerificationRoute {

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    public ResetPasswordRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;
    }

    @Override
    public JSONObject handleAdditional(JSONObject jsonObject, Response response) {
        String email = getEmail(jsonObject);

        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountData playerAccountData = gameDatabase.getPlayerAccountTable()
                .getPlayerAccountDataByEmail(connectionHandler, email);

        if (playerAccountData == null) {
            return getErrorObject("No account associated with this email.");
        }

        if (playerAccountData.getAccountVerificationStatus() != AccountVerificationStatus.VERIFIED) {
            return getErrorObject("Your account has not finished being setup yet.");
        }

        int playerId = playerAccountData.getPlayerId();
        String username = playerAccountData.getUsername();
        String password = jsonObject.getString("password");

        if (password == null) {
            return getErrorObject("Null password");
        }

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId, username, password);

        connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
        authenticationDatabase.getAuthenticationTable().updatePlayerAuthenticationRecord(connectionHandler,
                playerAuthenticationData);

        return getSuccessObject();
    }

}
