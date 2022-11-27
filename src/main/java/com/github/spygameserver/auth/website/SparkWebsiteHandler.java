package com.github.spygameserver.auth.website;

import com.github.spygameserver.auth.website.email.GetAccountUsernameRoute;
import com.github.spygameserver.auth.website.email.RegisterAccountRoute;
import com.github.spygameserver.auth.website.email.ResetPasswordRoute;
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
        //System.out.println(System.getProperty("user.dir") + File.separator + "public");
        //Spark.externalStaticFileLocation(System.getProperty("user.dir") + File.separator + "public");

        Spark.staticFileLocation("/public");

        Spark.path("/account", () -> {

            Spark.path("/email", () -> {
                Spark.post("/verify", new VerifyEmailAccountRoute(gameDatabase, authenticationDatabase));
                Spark.post("/disable", new DisablePlayerAccountRoute(gameDatabase, authenticationDatabase));
            });

            Spark.path("/username", () -> {
                    Spark.post("/get", new GetAccountUsernameRoute(gameDatabase));
                    Spark.get("/check/:username", new CheckUsernameExistsRoute(gameDatabase));
            });

            Spark.post("/register", new RegisterAccountRoute(gameDatabase, authenticationDatabase));

            Spark.post("/reset", new ResetPasswordRoute(gameDatabase, authenticationDatabase));
        });
    }

    public void shutdown() {
        Spark.stop();
    }

}
