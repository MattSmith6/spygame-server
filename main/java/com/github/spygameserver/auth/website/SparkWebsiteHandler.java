package com.github.spygameserver.auth.website;

import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import spark.Spark;

import java.io.File;

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

            Spark.path("/email", () -> Spark.post("/verify", new AddVerifiedEmailRoute(gameDatabase)));

            Spark.path("/username", () -> {
                    Spark.post("/get", new GetAccountUsernameRoute(gameDatabase));
                    Spark.get("/check/:username", new CheckUsernameExistsRoute(gameDatabase));
            });

            Spark.post("/register", new AddUsernameEmailRoute(gameDatabase, authenticationDatabase));

            Spark.post("/reset", new ResetPasswordRoute(gameDatabase, authenticationDatabase));
        });
    }

    public void shutdown() {
        Spark.stop();
    }

}
