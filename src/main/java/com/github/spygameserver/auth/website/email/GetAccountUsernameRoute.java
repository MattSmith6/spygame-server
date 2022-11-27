package com.github.spygameserver.auth.website.email;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Response;

public class GetAccountUsernameRoute extends EmailRequiredRoute {

    private final GameDatabase gameDatabase;

    public GetAccountUsernameRoute(GameDatabase gameDatabase) {
        this.gameDatabase = gameDatabase;
    }

    @Override
    public JSONObject handleAdditional(JSONObject jsonObject, String email, Response response) {
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
