package com.github.spygameserver.auth.website;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.AuthenticationTable;
import com.github.spygameserver.database.table.PlayerAccountTable;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * The test will break work once the token field is required for email verification, as these are dummy variables.
 * Ensuring that this test passes before implementation of the email verification allows testing to be conducted on the app,
 * while knowing that all server website functions work correctly when the verification works correctly.
 *
 * This test is designed to test the functionality of all the spark handler subprocesses:
 * 1. Adding a verified email (step 1/2 for account creation)
 * 2. Choosing a username and password (step 2/2 for account creation)
 * 3. Resetting password
 * 4. Recovering lost username
 * 5. Check username exists (for account screen integration)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SparkWebsiteHandlerTest implements DatabaseRequiredTest {

    private static final String RECOVER_USERNAME_PATH = "account/username/get";
    private static final String CHECK_USERNAME_PATH = "account/username/check/%s";
    private static final String RESET_PASSWORD_PATH = "account/reset";
    private static final String VERIFY_EMAIL_PATH = "account/email/verify";
    private static final String REGISTER_ACCOUNT_PATH = "account/register";

    private static final String VALID_EMAIL = "george@my.csun.edu";
    private static final String VALID_USERNAME = "georgie123";

    private static final String ORIGINAL_PASSWORD = "abcdefg";
    private static final String PASSWORD_AFTER_RESET = "hijklmnop";

    private GameDatabase gameDatabase;
    private AuthenticationDatabase authenticationDatabase;

    private PlayerAccountTable playerAccountTable;
    private AuthenticationTable authenticationTable;

    private SparkWebsiteHandler sparkWebsiteHandler;

    @BeforeAll
    public void setupDatabases() {
        gameDatabase = getGameDatabase();
        ConnectionHandler gameConnectionHandler = gameDatabase.getNewConnectionHandler(false);

        authenticationDatabase = getAuthenticationDatabase();
        ConnectionHandler authConnectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        playerAccountTable = gameDatabase.getPlayerAccountTable();
        authenticationTable = authenticationDatabase.getAuthenticationTable();

        sparkWebsiteHandler = new SparkWebsiteHandler(gameDatabase, authenticationDatabase);

        // Remove all data that should be unique so no testing errors occur
        playerAccountTable.dropTableSecure(gameConnectionHandler);
        playerAccountTable.initialize(gameConnectionHandler);


        authenticationTable.dropTableSecure(authConnectionHandler);
        authenticationTable.initialize(authConnectionHandler);

        gameConnectionHandler.closeAbsolutely();
        authConnectionHandler.closeAbsolutely();
    }

    private ConnectionHandler getGameConnectionHandler() {
        return gameDatabase.getNewConnectionHandler(true);
    }

    private ConnectionHandler getAuthConnectionHandler() {
        return authenticationDatabase.getNewConnectionHandler(true);
    }

    @Test
    @Order(1)
    public void testInvalidUsernameGetWithNoAccount() {
        createGetUsernameTest(withExpectedErrorMessage("You have not yet setup an account for Spy Game."));
    }

    @Test
    @Order(1)
    public void testInvalidPasswordResetWithNoAccount() {
        createResetPasswordTest(withExpectedErrorMessage("No account associated with this email."));
    }

    @Test
    @Order(1)
    public void testUsernameDoesNotExist() {
        createCheckUsernameDoesNotExistTest();
    }

    @Test
    @Order(2)
    public void testVerifyEmail() {
        createVerifyEmailTest(getFailOnErrorConsumer());

        PlayerAccountData realData = playerAccountTable.getPlayerAccountDataByEmail(getGameConnectionHandler(), VALID_EMAIL);
        PlayerAccountData expectedData = new PlayerAccountData(realData.getPlayerId(), VALID_EMAIL, null,
                AccountVerificationStatus.CHOOSE_USERNAME);

        Assertions.assertEquals(expectedData, realData);
    }

    @Test
    @Order(3)
    public void testInvalidUsernameGetWithOnlyVerifiedEmail() {
        createGetUsernameTest(withExpectedErrorMessage("You have not yet chosen a username for your Spy Game account."));
    }

    @Test
    @Order(3)
    public void testInvalidPasswordResetWithOnlyVerifiedEmail() {
        createResetPasswordTest(withExpectedErrorMessage("Your account has not finished being setup yet."));
    }

    @Test
    @Order(3)
    public void testInvalidVerifyEmailWithAlreadyVerifiedEmail() {
        createVerifyEmailTest(withExpectedErrorMessage("This email is already registered to another account, please sign in."));
    }

    @Test
    @Order(4)
    public void testValidRegisterAccount() {
        createRegisterAccountTest(getFailOnErrorConsumer());

        PlayerAccountData realAccountData = playerAccountTable.getPlayerAccountDataByEmail(getGameConnectionHandler(), VALID_EMAIL);
        PlayerAccountData expectedAccountData = new PlayerAccountData(realAccountData.getPlayerId(), VALID_EMAIL,
                VALID_USERNAME, AccountVerificationStatus.VERIFIED);

        Assertions.assertEquals(expectedAccountData, realAccountData);

        int playerId = expectedAccountData.getPlayerId();
        PlayerAuthenticationData authData = authenticationTable.getPlayerAuthenticationRecord(getAuthConnectionHandler(), playerId);

        // If the account was registered, the password should have generated authentication data
        Assertions.assertNotNull(authData);
    }

    @Test
    @Order(5)
    public void testUsernameDoesExist() {
        createCheckUsernameDoesExistTest();
    }

    @Test
    @Order(5)
    public void testValidUsernameGetOnCompletedAccount() {
        // Check if the username returned by the service matches the expected username
        createGetUsernameTest(jsonObject -> Assertions.assertEquals(jsonObject.getString("username"), VALID_USERNAME));
    }

    @Test
    @Order(5)
    public void testValidPasswordResetOnCompletedAccount() {
        int playerId = playerAccountTable.getPlayerIdByUsername(getGameConnectionHandler(), VALID_USERNAME);
        PlayerAuthenticationData dataPrePasswordReset = authenticationTable.getPlayerAuthenticationRecord(
                getAuthConnectionHandler(), playerId);

        createResetPasswordTest(getFailOnErrorConsumer());

        PlayerAuthenticationData dataPostPasswordReset = authenticationTable.getPlayerAuthenticationRecord(
                getAuthConnectionHandler(), playerId);

        // On a valid reset password, all authentication data is regenerated
        Assertions.assertNotEquals(dataPrePasswordReset, dataPostPasswordReset, "Authentication data should not match.");
    }

    @Test
    @Order(5)
    public void testInvalidRegisterOnCompletedAccount() {
        createRegisterAccountTest(withExpectedErrorMessage("This account has already chosen a username."));
    }

    private void createCheckUsernameDoesExistTest() {
        createCheckUsernameExistsTest(jsonObject -> Assertions.assertTrue(jsonObject.getBoolean("exists")));
    }

    private void createCheckUsernameDoesNotExistTest() {
        createCheckUsernameExistsTest(jsonObject -> Assertions.assertFalse(jsonObject.getBoolean("exists")));
    }

    private void createCheckUsernameExistsTest(Consumer<JSONObject> jsonObjectConsumer) {
        createGetTest(String.format(CHECK_USERNAME_PATH, VALID_USERNAME), jsonObjectConsumer);
    }

    private void createGetUsernameTest(Consumer<JSONObject> jsonObjectConsumer) {
        createPostTest(RECOVER_USERNAME_PATH, jsonObjectConsumer);
    }

    private void createGetTest(String path, Consumer<JSONObject> jsonObjectConsumer) {
        createGetTest(path, jsonObjectConsumer, null);
    }

    private void createGetTest(String path, Consumer<JSONObject> jsonObjectConsumer, Map<String, String> additionalParams) {
        createTest(path, jsonObjectConsumer, additionalParams, this::makeGetRequest);
    }

    private void createVerifyEmailTest(Consumer<JSONObject> jsonObjectConsumer) {
        createPostTest(VERIFY_EMAIL_PATH, jsonObjectConsumer);
    }

    private void createRegisterAccountTest(Consumer<JSONObject> jsonObjectConsumer) {
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("username", VALID_USERNAME);
        additionalParams.put("password", ORIGINAL_PASSWORD);

        createPostTest(REGISTER_ACCOUNT_PATH, jsonObjectConsumer, additionalParams);
    }

    private void createResetPasswordTest(Consumer<JSONObject> jsonObjectConsumer) {
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("password", PASSWORD_AFTER_RESET);

        createPostTest(RESET_PASSWORD_PATH, jsonObjectConsumer, additionalParams);
    }

    private void createPostTest(String path, Consumer<JSONObject> jsonObjectConsumer) {
        createPostTest(path, jsonObjectConsumer, null);
    }

    private void createPostTest(String path, Consumer<JSONObject> jsonObjectConsumer, Map<String, String> additionalParams) {
        createTest(path, jsonObjectConsumer, additionalParams, this::makePostRequest);
    }

    private void createTest(String path, Consumer<JSONObject> jsonObjectConsumer, Map<String, String> additionalParams,
                            BiFunction<String, Map<String, String>, ClassicHttpRequest> requestFunction) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Map<String, String> parameters = getParameterMapWithEmail();
            if (additionalParams != null) {
                parameters.putAll(additionalParams);
            }

            ClassicHttpRequest httpRequest = requestFunction.apply(path, parameters);

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                HttpEntity httpEntity = response.getEntity();
                JSONObject jsonObject = getJSONFromEntity(httpEntity);

                Assertions.assertNotNull(jsonObject, "JSONObject should never be null");
                jsonObjectConsumer.accept(jsonObject);

                EntityUtils.consume(httpEntity);
            }
        } catch (IOException ex) {
            Assertions.fail(ex);
        }
    }

    private Consumer<JSONObject> withExpectedErrorMessage(String errorMessage) {
        return jsonObject -> Assertions.assertEquals(jsonObject.getString("error"), errorMessage);
    }

    private Consumer<JSONObject> getFailOnErrorConsumer() {
        return jsonObject -> {
            if (jsonObject.getString("status").equals("ERROR")) {
                Assertions.fail("Bad request: " + jsonObject.getString("error"));
            }
        };
    }

    private JSONObject getJSONFromEntity(HttpEntity httpEntity) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpEntity.getContent()))) {
            StringBuilder jsonStringBuilder = new StringBuilder();
            bufferedReader.lines().forEach(jsonStringBuilder::append);

            String stringifiedJSON = jsonStringBuilder.toString();
            JSONTokener jsonTokener = new JSONTokener(stringifiedJSON);
            return new JSONObject(jsonTokener);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private Map<String, String> getParameterMapWithEmail() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("email", VALID_EMAIL);

        return parameterMap;
    }

    private HttpGet makeGetRequest(String path, Map<String, String> keyValuePairs) {
        return new HttpGet(getUrlFromPath(path));
    }

    private HttpPost makePostRequest(String path, Map<String, String> keyValuePairs) {
        HttpPost postRequest = new HttpPost(getUrlFromPath(path));
        postRequest.setEntity(getEntity(keyValuePairs));

        return postRequest;
    }

    private String getUrlFromPath(String path) {
        return "http://localhost:80/" + path;
    }

    private HttpEntity getEntity(Map<String, String> keyValuePairs) {
        return new UrlEncodedFormEntity(getNameValuePairList(keyValuePairs));
    }

    private List<NameValuePair> getNameValuePairList(Map<String, String> keyValuePairs) {
        List<NameValuePair> nameValuePairs = new ArrayList<>(keyValuePairs.size());

        for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return nameValuePairs;
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        sparkWebsiteHandler.shutdown();
    }

}
