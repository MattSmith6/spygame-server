package com.github.spygameserver.auth.website;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.PlayerAccountTable;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A Route that
 */
public class CheckUsernameExistsRoute implements Route {

    private final GameDatabase gameDatabase;
    private final PlayerAccountTable playerAccountTable;

    public CheckUsernameExistsRoute(GameDatabase gameDatabase) {
        this.gameDatabase = gameDatabase;
        this.playerAccountTable = gameDatabase.getPlayerAccountTable();
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");

        String usernameToCheck = request.params("username");
        JSONObject statusObject = new JSONObject();

        // Put error status temporarily
        statusObject.put("status", "ERROR");

        // If the username is null, return an error
        if (usernameToCheck == null) {
            statusObject.put("error", "Username to check is null.");
            return statusObject.toString();
        }

        // If the username is an invalid length, return an error
        if (usernameToCheck.length() > 16) {
            statusObject.put("error", "Invalid username length.");
            return statusObject.toString();
        }

        // Return a success object with a boolean for whether the username already exists in the database
        statusObject.put("status", "SUCCESS");
        statusObject.put("exists", playerAccountTable.getPlayerIdByUsername(getNewConnection(), usernameToCheck) != null);

        return statusObject.toString();
    }

    private ConnectionHandler getNewConnection() {
        return gameDatabase.getNewConnectionHandler(true);
    }

}
