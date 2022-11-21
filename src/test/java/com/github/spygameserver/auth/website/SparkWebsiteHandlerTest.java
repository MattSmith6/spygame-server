package com.github.spygameserver.auth.website;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SparkWebsiteHandlerTest implements DatabaseRequiredTest {

    private GameDatabase gameDatabase;
    private AuthenticationDatabase authenticationDatabase;

    private SparkWebsiteHandler sparkWebsiteHandler;

    @BeforeAll
    public void setupDatabases() {
        gameDatabase = getGameDatabase();
        authenticationDatabase = getAuthenticationDatabase();

        sparkWebsiteHandler = new SparkWebsiteHandler(gameDatabase, authenticationDatabase);
    }

    @AfterAll
    @Override
    public void closeOpenConnections() {
        sparkWebsiteHandler.shutdown();
    }

}
