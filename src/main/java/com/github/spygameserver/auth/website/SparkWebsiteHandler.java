package com.github.spygameserver.auth.website;

import com.github.spygameserver.auth.website.email.RequestAccountUsernameRoute;
import com.github.spygameserver.auth.website.email.RegisterAccountRoute;
import com.github.spygameserver.auth.website.email.RequestPasswordResetRoute;
import com.github.spygameserver.auth.website.token.ResetPasswordRoute;
import com.github.spygameserver.auth.website.token.DisablePlayerAccountRoute;
import com.github.spygameserver.auth.website.token.VerifyEmailAccountRoute;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import spark.Spark;

public class SparkWebsiteHandler {

    public SparkWebsiteHandler(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        setupVerifyEmailPostRequest(gameDatabase, authenticationDatabase);
    }

    private void setupVerifyEmailPostRequest(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        Spark.port(80);

        Spark.staticFileLocation("/public");

        Spark.path("/account", () -> {

            Spark.path("/email", () -> {
                Spark.get("/verify", new VerifyEmailAccountRoute(gameDatabase, authenticationDatabase));
                Spark.get("/disable", new DisablePlayerAccountRoute(gameDatabase, authenticationDatabase));
            });

            Spark.path("/username", () -> {
                    Spark.post("/request", new RequestAccountUsernameRoute(gameDatabase));
                    Spark.get("/check/:username", new CheckUsernameExistsRoute(gameDatabase));
            });

            Spark.post("/register", new RegisterAccountRoute(gameDatabase, authenticationDatabase));

            Spark.path("/reset", () -> {
                Spark.post("/request", new RequestPasswordResetRoute(gameDatabase, authenticationDatabase));
                Spark.post("/doReset", new ResetPasswordRoute(gameDatabase, authenticationDatabase));
            });
        });
    }

    public void shutdown() {
        Spark.stop();
    }

}
