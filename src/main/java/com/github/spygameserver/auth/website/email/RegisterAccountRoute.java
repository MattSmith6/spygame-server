package com.github.spygameserver.auth.website.email;

import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.json.JSONObject;
import spark.Response;

import java.util.regex.Pattern;

public class RegisterAccountRoute extends EmailRequiredRoute {

    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    public RegisterAccountRoute(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;
    }

    @Override
    public JSONObject handleAdditional(JSONObject jsonObject, String email, Response response) {
        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerAccountTable playerAccountTable = gameDatabase.getPlayerAccountTable();

        PlayerAccountData playerAccountData = playerAccountTable.getPlayerAccountDataByEmail(connectionHandler, email);

        // If there is already a registered account that is not disabled, check inbox
        if (playerAccountData != null && playerAccountData.getAccountVerificationStatus() != AccountVerificationStatus.DISABLED) {
            return getErrorObject("You have an email in your inbox, please finish verification there.");
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

        boolean doesUsernameMatchAccount = playerAccountData != null && playerAccountData.getUsername().equals(username);

        // If the account table already exists
        if (playerAccountTable.doesUsernameAlreadyExist(connectionHandler, username) &&
                (doesUsernameMatchAccount || playerAccountData == null)) {
            return getErrorObject("This username already exists for another account.");
        }

        String password = jsonObject.getString("password");

        if (password == null) {
            return getErrorObject("Null password");
        }

        // Add player account to database
        connectionHandler = gameDatabase.getNewConnectionHandler(true);

        int playerId;
        if (playerAccountData == null) {
            playerId = playerAccountTable.createPlayerAccount(connectionHandler, email, username);
        } else {
            playerId = playerAccountData.getPlayerId();
            playerAccountTable.setUpdateDisabledAccount(connectionHandler, username, playerId);
        }

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId, username, password);

        // Add authentication information to the database
        connectionHandler = authenticationDatabase.getNewConnectionHandler(true);

        if (playerAccountData == null) {
            authenticationDatabase.getAuthenticationTable().addPlayerAuthenticationRecord(connectionHandler,
                    playerAuthenticationData);
        } else {
            authenticationDatabase.getAuthenticationTable().updatePlayerAuthenticationRecord(connectionHandler,
                    playerAuthenticationData);
        }

        return getSuccessObject();
    }

}
