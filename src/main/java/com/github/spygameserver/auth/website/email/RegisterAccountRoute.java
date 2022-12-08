package com.github.spygameserver.auth.website.email;

import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.email.VerifyOrDisableEmailCreator;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.apache.commons.mail.EmailException;
import org.json.JSONObject;
import spark.Response;

import java.util.regex.Pattern;

/**
 * The Spark Route used to register an account with a CSUN email. Requires
 */
public class RegisterAccountRoute extends EmailRequiredRoute {

    /**
     * Regex pattern allowing for only a-z, A-Z, and 0-9.
     */
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

        // Several invalid username checks

        if (username == null) {
            return getErrorObject("Null username");
        }

        if (username.length() < 5 || username.length() > 16) {
            return getErrorObject("Invalid username length");
        }

        if (!VALID_USERNAME_PATTERN.matcher(username).matches()) {
            return getErrorObject("Invalid username characters.");
        }

        connectionHandler = gameDatabase.getNewConnectionHandler(true);

        boolean doesUsernameMatchAccount = playerAccountData != null && playerAccountData.getUsername().equals(username);

        // If the username already exists and it's not the username for the possibly disabled account, error
        if (playerAccountTable.doesUsernameAlreadyExist(connectionHandler, username) &&
                (doesUsernameMatchAccount || playerAccountData == null)) {
            return getErrorObject("This username already exists for another account.");
        }

        String password = jsonObject.getString("password");

        if (password == null) {
            return getErrorObject("Null password");
        }

        connectionHandler = gameDatabase.getNewConnectionHandler(true);

        // Add player account to database if it doesn't exist, update the disabled account if it does
        int playerId;
        if (playerAccountData == null) {
            playerId = playerAccountTable.createPlayerAccount(connectionHandler, email, username);
        } else {
            playerId = playerAccountData.getPlayerId();
            playerAccountTable.updateDisabledAccount(connectionHandler, username, playerId);
        }

        PlayerAuthenticationData playerAuthenticationData = new PlayerAuthenticationData(playerId, username, password);
        connectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        // Add the authentication if doesn't exist, or update it for a disabled account
        if (playerAccountData == null) {
            authenticationDatabase.getAuthenticationTable().addPlayerAuthenticationRecord(connectionHandler,
                    playerAuthenticationData);
        } else {
            authenticationDatabase.getAuthenticationTable().updatePlayerAuthenticationRecord(connectionHandler,
                    playerAuthenticationData);
        }

        // Gneerate a new token
        String verificationToken = authenticationDatabase.getVerificationTokenTable()
                .addNewVerificationTokenForPlayer(connectionHandler, playerId);

        connectionHandler.closeAbsolutely();

        // Send the email for verification or disabled with the randomly generated token
        try {
            new VerifyOrDisableEmailCreator(email, verificationToken).sendNewEmail();
        } catch (EmailException ex) {
            ex.printStackTrace();
        }

        return getSuccessObject();
    }

}
