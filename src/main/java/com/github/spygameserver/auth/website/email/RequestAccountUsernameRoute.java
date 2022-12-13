package com.github.spygameserver.auth.website.email;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A Route that gets the username for a provided email, if it exists in the database.
 */
public class RequestAccountUsernameRoute implements Route {

    private final GameDatabase gameDatabase;

    public RequestAccountUsernameRoute(GameDatabase gameDatabase) {
        this.gameDatabase = gameDatabase;
    }

    @Override
    public Object handle(Request request, Response response) {
        String email = request.queryParams("email");

        // Return with an error status if no email
        if (email == null) {
            response.status(403);
            return null;
        }

        response.type("application/json");

        // Get the account data for the provided email
        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountData playerAccountData = gameDatabase.getPlayerAccountTable()
                .getPlayerAccountDataByEmail(connectionHandler, email);

        JSONObject responseObject = new JSONObject();
        responseObject.put("status", "ERROR");

        // If the account data is null, respond with the error message
        if (playerAccountData == null) {
            responseObject.put("error", "You have not yet setup an account for Spy Game.");
            return responseObject;
        }

        // Return the success object
        responseObject.put("status", "SUCCESS");
        responseObject.put("username", playerAccountData.getUsername());

        return responseObject;
    }

}
