package com.github.spygameserver.auth.website;

import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import spark.ExceptionHandlerImpl;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Arrays;

public class SparkWebsiteHandler {

    public SparkWebsiteHandler(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        setupVerifyEmailPostRequest(gameDatabase, authenticationDatabase);
    }

    private void setupVerifyEmailPostRequest(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        Spark.path("/account", () -> {

            Spark.path("/email", () -> Spark.post("/verify", new AddVerifiedEmailRoute(gameDatabase)));

            Spark.path("/username", () -> Spark.post("/get", new GetAccountUsernameRoute(gameDatabase)));

            Spark.post("/register", new AddUsernameEmailRoute(gameDatabase, authenticationDatabase));

            Spark.post("/reset", new ResetPasswordRoute(gameDatabase, authenticationDatabase));
        });

        Spark.exception(Exception.class, (ex, req, res) -> ex.printStackTrace());
    }

    public void shutdown() {
        Spark.stop();
    }

}
