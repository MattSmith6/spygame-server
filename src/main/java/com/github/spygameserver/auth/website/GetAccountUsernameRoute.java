package com.github.spygameserver.auth.website;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Response;

public class GetAccountUsernameRoute extends VerificationRoute {

    private final GameDatabase gameDatabase;

    public GetAccountUsernameRoute(GameDatabase gameDatabase) {
        this.gameDatabase = gameDatabase;
    }

    @Override
    public JSONObject handleAdditional(JSONObject requestBody, Response response) {
        String email = requestBody.getString("email");

        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountData playerAccountData = gameDatabase.getPlayerAccountTable()
                .getPlayerAccountDataByEmail(connectionHandler, email);

        if (playerAccountData == null) {
            return getErrorObject("You have not yet setup an account for Spy Game.");
        }

        if (playerAccountData.getAccountVerificationStatus() != AccountVerificationStatus.VERIFIED) {
            return getErrorObject("You have not yet chosen a username for your Spy Game account.");
        }

        JSONObject successObject = getSuccessObject();
        successObject.put("username", playerAccountData.getUsername());

        return successObject;
    }

}
