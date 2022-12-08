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

/**
 * A class that handles the generation of the website using the Spark java library.
 * In Spark, a Route is when a user connects to a URL at a certain specified path (e.g. account/email/verify).
 * The implementations of Route are mapped to their respective paths, as these features do not require credential verification.
 * Therefore, these services were moved to a website instead of packets, as that way email links can be sent easily.
 */
public class SparkWebsiteHandler {

    public SparkWebsiteHandler(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        setupVerifyEmailPostRequest(gameDatabase, authenticationDatabase);
    }

    private void setupVerifyEmailPostRequest(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        Spark.port(80);

        Spark.staticFileLocation("/public");

        Spark.path("/account", () -> {

            Spark.path("/email", () -> {
                // Sets the AccountVerificationStatus to be VERIFIED, for a given token and email
                Spark.get("/verify", new VerifyEmailAccountRoute(gameDatabase, authenticationDatabase));

                // Sets the AccountVerificationStatus to be DISABLED, for a given token and email
                Spark.get("/disable", new DisablePlayerAccountRoute(gameDatabase, authenticationDatabase));
            });

            Spark.path("/username", () -> {
                    // Gets the username for the specified account (forgot username), for a given email
                    Spark.get("/request", new RequestAccountUsernameRoute(gameDatabase));

                    // Checks if the username already exists (used when looking at the form)
                    Spark.get("/check/:username", new CheckUsernameExistsRoute(gameDatabase));
            });

            // Registers the account with a given email, username, and password, and sends link for verify/disable
            Spark.post("/register", new RegisterAccountRoute(gameDatabase, authenticationDatabase));

            Spark.path("/reset", () -> {
                // Make email request to provide a link and token for password reset
                Spark.post("/request", new RequestPasswordResetRoute(gameDatabase, authenticationDatabase));

                // Take the form data from the provided link, and post/update the database if necessary
                Spark.post("/doReset", new ResetPasswordRoute(gameDatabase, authenticationDatabase));
            });
        });
    }

    public void shutdown() {
        Spark.stop();
    }

}
