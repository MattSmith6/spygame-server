package com.github.spygameserver.auth.website.email;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

public class RequestAccountUsernameRoute implements Route {

    private final GameDatabase gameDatabase;

    public RequestAccountUsernameRoute(GameDatabase gameDatabase) {
        this.gameDatabase = gameDatabase;
    }

    @Override
    public Object handle(Request request, Response response) {
        String email = request.queryParams("email");

        if (email == null) {
            response.status(403);
            return null;
        }

        response.type("application/json");

        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountData playerAccountData = gameDatabase.getPlayerAccountTable()
                .getPlayerAccountDataByEmail(connectionHandler, email);

        JSONObject responseObject = new JSONObject();
        responseObject.put("status", "ERROR");

        if (playerAccountData == null) {
            responseObject.put("error", "You have not yet setup an account for Spy Game.");
            return responseObject;
        }

        responseObject.put("status", "SUCCESS");
        responseObject.put("username", playerAccountData.getUsername());

        return responseObject;
    }

}
