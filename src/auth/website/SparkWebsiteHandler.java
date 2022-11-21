package com.github.spygameserver.auth.website;

import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import spark.Spark;

public class SparkWebsiteHandler {

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    public SparkWebsiteHandler(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;
    }

    public void initialize() {

    }

    private void setupVerifyEmailPostRequest() {
        Spark.path("/verification", () -> {
            //Spark.put("/email", );
            //Spark.put("/username", );
        });
    }

}
