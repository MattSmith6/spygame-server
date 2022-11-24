package com.github.spygameserver.auth.website;

import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Response;

import java.nio.ByteOrder;
import java.util.Map;
import java.util.regex.Pattern;

public class AddUsernameEmailRoute extends VerificationRoute {

    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    public AddUsernameEmailRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;
    }

    @Override
    public JSONObject handleAdditional(JSONObject jsonObject, Response response) {
        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountTable playerAccountTable = gameDatabase.getPlayerAccountTable();

        String email = getEmail(jsonObject);
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

        String username = jsonObject.getString("username");

        if (username == null) {
            return getErrorObject("Null username");
        }

        if (username.length() > 16) {
            return getErrorObject("Invalid username length");
        }

        if (!VALID_USERNAME_PATTERN.matcher(username).matches()) {
            return getErrorObject("Invalid username characters.");
        }

        connectionHandler = gameDatabase.getNewConnectionHandler(true);

        // If the account table already exists
        if (playerAccountTable.doesUsernameAlreadyExist(connectionHandler, username)) {
            return getErrorObject("This username already exists for another account.");
        }

        int playerId = playerAccountData.getPlayerId();
        String password = jsonObject.getString("password");

        if (password == null) {
            return getErrorObject("Null password");
        }

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId, username, password);

        // Add authentication information to the database
        connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
        authenticationDatabase.getAuthenticationTable().addPlayerAuthenticationRecord(connectionHandler,
                playerAuthenticationData);

        // Add username to the database
        connectionHandler = gameDatabase.getNewConnectionHandler(true);
        playerAccountTable.addUsernameToPlayerAccount(connectionHandler, email, username);

        return getSuccessObject();
    }

}
