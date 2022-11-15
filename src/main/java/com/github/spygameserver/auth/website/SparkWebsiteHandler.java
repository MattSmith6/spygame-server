package com.github.spygameserver.auth.website;

import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import spark.Spark;

public class SparkWebsiteHandler {

    public SparkWebsiteHandler(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        setupVerifyEmailPostRequest(gameDatabase, authenticationDatabase);
    }

    private void setupVerifyEmailPostRequest(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        Spark.path("/account", () -> {

            Spark.path("/email", () -> Spark.put("/verify", new AddVerifiedEmailRoute(gameDatabase)));

            Spark.path("/username", () -> Spark.put("/get", new GetAccountUsernameRoute(gameDatabase)));

            Spark.put("/register", new AddUsernameEmailRoute(gameDatabase, authenticationDatabase));

            Spark.put("/reset", new ResetPasswordRoute(gameDatabase, authenticationDatabase));

        });
    }

}
